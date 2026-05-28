# Web Design Guidelines Skill

Source: https://github.com/vercel-labs/agent-skills/blob/main/skills/web-design-guidelines/SKILL.md

## Overview

This skill validates UI code against established Web Interface Guidelines maintained by Vercel Labs. It performs automated design reviews to ensure compliance with web best practices.

## When to Use

Trigger this skill when the user says:
- "review my UI"
- "check accessibility"
- "audit design"
- "review UX"
- "check my site against best practices"
- "validate my interface"

## Core Functionality

The skill:
1. Fetches the latest guidelines from the Vercel Labs guidelines source
2. Reads the specified files
3. Validates them against all documented rules
4. Reports findings in compact `file:line` format

**Guidelines source URL:**
```
https://raw.githubusercontent.com/vercel-labs/web-interface-guidelines/main/command.md
```

Always fetch fresh guidelines before each review session to ensure up-to-date rules.

## Workflow

**Step 1:** Receive file paths or patterns to review (ask if not provided)

**Step 2:** Fetch latest guidelines:
```
WebFetch: https://raw.githubusercontent.com/vercel-labs/web-interface-guidelines/main/command.md
```

**Step 3:** Read each specified file

**Step 4:** Apply all rules from the fetched guidelines to each file

**Step 5:** Report findings in format: `file:line — rule description`

## Output Format

```
src/components/Button.tsx:42 — touch target below 44px minimum
src/pages/Home.css:18 — color contrast ratio insufficient (3.2:1, minimum 4.5:1)
src/components/Modal.tsx:91 — missing focus trap implementation
```

If no issues found: "No violations found in the reviewed files."

## File Input

- Accept specific file paths: `src/components/Button.tsx`
- Accept glob patterns: `src/components/**/*.tsx`
- If no files specified, ask: "Which files would you like me to review against the Web Interface Guidelines?"
