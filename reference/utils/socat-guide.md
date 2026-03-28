# socat Guide — Install, Use, Debug, Maintain

socat ("SOcket CAT") is a bidirectional data relay. In a Kubernetes / kind context it is the simplest way to bridge a host port (80, 443) to a NodePort without touching cluster networking or iptables.

---

## 1. Install

### Ubuntu / Debian
```bash
sudo apt-get update && sudo apt-get install -y socat
```

### RHEL / CentOS / Amazon Linux
```bash
sudo yum install -y socat          # RHEL 7 / CentOS 7
sudo dnf install -y socat          # RHEL 8+ / Amazon Linux 2023
```

### Verify
```bash
socat -V
# socat version 1.7.x.x
```

---

## 2. Basic usage pattern

```
socat TCP-LISTEN:<host-port>,fork TCP:<target-ip>:<target-port>
```

| Argument | Meaning |
|---|---|
| `TCP-LISTEN:<port>` | Bind and listen on the given host port |
| `fork` | Spawn a child process per connection (required for concurrent clients) |
| `TCP:<ip>:<port>` | Forward each connection to this address |

### Example — forward host port 80 → kind NodePort 30423
```bash
sudo socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423
```

> Ports < 1024 require `sudo`.

---

## 3. Run modes

### Foreground (testing only)
```bash
sudo socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423
# Ctrl+C to stop
```
Process dies when the terminal is closed. **Do not use in production.**

### Background with `&` (also unreliable)
```bash
sudo socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423 &
```
Killed or suspended (`T` state) when the parent shell exits or receives `SIGHUP`.

### Background with `nohup` (recommended for long-running)
```bash
sudo nohup socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423 \
  > /var/log/socat-80.log 2>&1 &

echo $!   # print PID so you can track it
```
`nohup` detaches from the terminal so closing the SSH session does not suspend or kill the process.

### Verify it is listening
```bash
sudo ss -tlnp | grep ':80'
# or
sudo lsof -i :80
```

---

## 4. HTTPS / port 443 (TLS passthrough)

The same pattern applies — socat just forwards raw TCP bytes, so TLS is terminated by nginx inside the cluster:

```bash
sudo nohup socat TCP-LISTEN:443,fork TCP:172.19.0.9:30424 \
  > /var/log/socat-443.log 2>&1 &
```

Replace `30424` with the actual HTTPS NodePort (`kubectl get svc -n ingress-nginx`).

---

## 5. Process lifecycle and debug

### List all running socat processes
```bash
ps aux | grep socat | grep -v grep
```

Example output — healthy:
```
USER   PID  %CPU %MEM  ...  STAT  ...  COMMAND
root  4521   0.0  0.0  ...  S     ...  sudo socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423
```

Example output — **suspended (broken)**:
```
USER    PID  %CPU %MEM  ...  STAT  ...  COMMAND
root  123448   0.0  0.0  ...  T     ...  sudo socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423
root  123549   0.0  0.0  ...  T     ...  sudo socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423
```

---

### Understanding the STAT column

`ps aux` prints a `STAT` (or `S`) column. The first character is the process state:

| STAT letter | Meaning |
|---|---|
| `S` | Sleeping — waiting for I/O or a connection, **healthy** |
| `R` | Running — actively on CPU |
| `T` | **Stopped** — suspended by job control, **not forwarding any traffic** |
| `Z` | Zombie — exited but parent has not collected status |

Additional modifier characters sometimes follow (e.g. `Ss`, `T+`):

| Modifier | Meaning |
|---|---|
| `s` | Session leader |
| `+` | In the foreground process group |
| `l` | Multi-threaded |
| `<` | High priority |

The critical letter to watch is the **first one**. `T` in first position always means stopped.

---

### The suspend problem — what happens and why

When you launch socat without `nohup`, the process belongs to the shell's **job control group**. Linux uses signals to manage job lifecycle:

| Signal | Number | Sent when |
|---|---|---|
| `SIGTSTP` | 20 | User presses `Ctrl+Z` in a terminal |
| `SIGHUP` | 1 | Terminal closes or SSH session disconnects |
| `SIGCONT` | 18 | Resume a stopped process (`fg` / `bg` commands) |

When the terminal or SSH session closes:
1. The shell sends `SIGHUP` to its process group.
2. Background jobs started with `&` receive `SIGHUP` and either terminate or, if they caught it, may survive.
3. Foreground jobs receive `SIGTSTP` first (via the terminal driver) before the session ends, putting them in state `T`.

In practice, socat started with bare `&` in an SSH session often ends up in `T` state when that session disconnects, because:
- The terminal driver sends `SIGTSTP` during session teardown
- socat does not handle `SIGTSTP` specially — it simply stops

The process still holds its socket file descriptor and the bound port, so the OS refuses a new `bind()` call on the same port — your next socat start will fail with `Address already in use` unless you kill the suspended process first.

---

### Step-by-step: diagnose a suspected suspend

**Step 1 — check process state**
```bash
ps aux | grep socat | grep -v grep
```
Look at the `STAT` column. `T` = suspended. `S` = healthy.

**Step 2 — confirm the port is bound but not accepting**
```bash
sudo ss -tlnp | grep ':80'
```
A suspended socat still shows up here (port bound), but connections will hang indefinitely rather than being accepted.

**Step 3 — try to connect locally and observe the hang**
```bash
curl --max-time 5 http://localhost/
# Expected from healthy socat: response or nginx error within 1s
# Symptom of suspended socat: curl hangs for exactly 5s then times out
```

**Step 4 — confirm from outside the server**
```bash
# From your laptop or CI
curl --max-time 5 http://api.staging.blitz-pay.com/
# Same hang = socat is suspended, not nginx
```

**Step 5 — check for multiple suspended copies**

Closing and reopening SSH sessions without cleanup accumulates suspended processes:
```bash
ps aux | grep socat | grep -v grep | wc -l
# If > 1, you have stale copies — kill all before restarting
```

---

### Quick connectivity test (from the server itself)
```bash
curl -v -H "Host: api.staging.blitz-pay.com" http://localhost/swagger-ui/index.html
```

### End-to-end test (from a client or CI)
```bash
curl -v http://api.staging.blitz-pay.com/swagger-ui/index.html
```

---

## 6. Restart procedure

Use this sequence whenever socat is unresponsive or in `T` state:

```bash
# 1. Kill every socat process (SIGKILL to bypass suspend state)
sudo pkill -9 socat

# 2. Confirm they are gone
ps aux | grep socat | grep -v grep   # should return nothing

# 3. Confirm the ports are free
sudo ss -tlnp | grep -E ':80|:443'

# 4. Restart with nohup
sudo nohup socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423 \
  > /var/log/socat-80.log 2>&1 &

sudo nohup socat TCP-LISTEN:443,fork TCP:172.19.0.9:30424 \
  > /var/log/socat-443.log 2>&1 &

# 5. Verify
ps aux | grep socat | grep -v grep
sudo ss -tlnp | grep -E ':80|:443'
```

---

## 7. Find the correct NodePorts

```bash
kubectl get svc -n ingress-nginx
```

Example output:
```
NAME                       TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)
ingress-nginx-controller   NodePort   10.96.x.x      <none>        80:30423/TCP,443:30424/TCP
```

Find the kind node IP:
```bash
kubectl get nodes -o wide
# or
docker inspect <kind-node-container-name> | grep IPAddress
```

---

## 8. Make socat survive reboots (systemd)

For a server that restarts regularly, manage socat via systemd instead of nohup.

Create `/etc/systemd/system/socat-http.service`:
```ini
[Unit]
Description=socat HTTP port bridge (80 → kind ingress NodePort)
After=network.target

[Service]
ExecStart=/usr/bin/socat TCP-LISTEN:80,fork TCP:172.19.0.9:30423
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now socat-http
sudo systemctl status socat-http
```

View logs:
```bash
sudo journalctl -u socat-http -f
```

Do the same for port 443 (`socat-https.service`).

> **Note:** Update `172.19.0.9:30423` if the kind node IP or NodePort changes (e.g., after cluster recreation). Then `sudo systemctl restart socat-http`.

---

## 9. Long-term: eliminate socat with kind extraPortMappings

socat is a workaround. The clean solution is to configure port mappings when the kind cluster is created so the host ports are forwarded automatically by Docker:

```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 30423
        hostPort: 80
        protocol: TCP
      - containerPort: 30424
        hostPort: 443
        protocol: TCP
```

See `reference/k8s-ingress-troubleshooting/03-terraform-kind-ingress.md` for Terraform-managed version.

---

## 10. Related references

- `reference/k8s-ingress-troubleshooting/01-commands-reference.md` — kubectl ingress diagnostics
- `reference/k8s-ingress-troubleshooting/02-issue-summary.md` — past ingress issues and resolutions
- `reference/k8s-ingress-troubleshooting/03-terraform-kind-ingress.md` — Terraform kind cluster with extraPortMappings
