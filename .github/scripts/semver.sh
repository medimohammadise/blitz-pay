#!/usr/bin/env bash
# semver.sh — Custom semantic versioning per https://semver.org/
# Usage: Called by GitHub Actions release workflow.
# Environment variables consumed:
#   PR_LABELS  — space- or newline-separated list of labels on the merged PR
#   GITHUB_OUTPUT — file path provided by the runner for step outputs
set -euo pipefail

# ---------------------------------------------------------------------------
# 1. Read the current (latest) version from git tags
# ---------------------------------------------------------------------------
latest_tag=$(git tag --list 'v[0-9]*' --sort=-version:refname | head -n1 || true)

if [[ -z "$latest_tag" ]]; then
  current_version="0.0.0"
else
  # Strip leading 'v'
  current_version="${latest_tag#v}"
fi

echo "Current version: $current_version"

# ---------------------------------------------------------------------------
# 2. Parse MAJOR.MINOR.PATCH (ignore pre-release / build metadata suffixes)
# ---------------------------------------------------------------------------
# Strip any pre-release or build-metadata suffix (e.g. 1.2.3-alpha+001 → 1.2.3)
core_version="${current_version%%-*}"
core_version="${core_version%%+*}"

IFS='.' read -r -a parts <<< "$core_version"
IFS=$' \t\n'
major="${parts[0]:-0}"
minor="${parts[1]:-0}"
patch="${parts[2]:-0}"

# Validate that each part is a non-negative integer
for part in "$major" "$minor" "$patch"; do
  if ! [[ "$part" =~ ^[0-9]+$ ]]; then
    echo "ERROR: Unable to parse version '${current_version}'" >&2
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# 3. Determine bump type from PR labels (highest priority wins)
# ---------------------------------------------------------------------------
bump_type="patch"   # default

# PR_LABELS may contain newlines or spaces; normalise to space-separated
labels_normalised=$(echo "${PR_LABELS:-}" | tr '\n' ' ')

if echo "$labels_normalised" | grep -qw "breaking-change"; then
  bump_type="major"
elif echo "$labels_normalised" | grep -qw "feature"; then
  bump_type="minor"
elif echo "$labels_normalised" | grep -qwE "bug-fix|tests|config|dependencies|documentation|infrastructure"; then
  bump_type="patch"
fi

echo "Labels: ${labels_normalised:-<none>}"
echo "Bump type: $bump_type"

# ---------------------------------------------------------------------------
# 4. Calculate new version
# ---------------------------------------------------------------------------
if [[ "$current_version" == "0.0.0" ]] && [[ -z "$latest_tag" ]]; then
  # No prior tags — use conventional first-release values
  case "$bump_type" in
    major) new_version="1.0.0" ;;
    minor) new_version="0.1.0" ;;
    patch) new_version="0.0.1" ;;
  esac
else
  case "$bump_type" in
    major)
      new_major=$(( major + 1 ))
      new_version="${new_major}.0.0"
      ;;
    minor)
      new_minor=$(( minor + 1 ))
      new_version="${major}.${new_minor}.0"
      ;;
    patch)
      new_patch=$(( patch + 1 ))
      new_version="${major}.${minor}.${new_patch}"
      ;;
  esac
fi

echo "New version: $new_version"

# ---------------------------------------------------------------------------
# 5. Write outputs
# ---------------------------------------------------------------------------
if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  echo "new_version=${new_version}" >> "$GITHUB_OUTPUT"
  echo "bump_type=${bump_type}" >> "$GITHUB_OUTPUT"
else
  # Fallback for local testing
  echo "new_version=${new_version}"
  echo "bump_type=${bump_type}"
fi
