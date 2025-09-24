---
name: cherry-pick
about: Describe this issue template's purpose here.
title: ''
labels: ''
assignees: ''

---

---
name: CherryPick Track
about: Track tasks when release branches need cherry-pick.
labels: help wanted
---

**Which PR needs cherry-picks:**
<!--
For example: "PR #1234"
-->
PR #

**Which release branches need this patch:**
<!--
If a branch doesn't need this cherry-pick, please explain the reason.
-->
- [ ] release-1.x 
- [ ] release-1.y
- [ ] release-1.z

**How to cherry-pick PRs:**

Please follow the standard git cherry-pick process:
1. Checkout the target release branch
2. Cherry-pick the commit: `git cherry-pick <commit-hash>`
3. Push the changes and create a PR

The script will send the PR for you, please remember `copy the release notes` from
the original PR by to the new PR description part.

**How to join or take the task**:

Just reply on the issue with the message `/assign` in a **separate line**.

Then, the issue will be assigned to you.

**Useful References:**

- Release timeline: https://github.com/jd-opensource/JoySafety/releases
- How to cherry-pick PRs: Standard git cherry-pick workflow

**Anything else we need to know:**
