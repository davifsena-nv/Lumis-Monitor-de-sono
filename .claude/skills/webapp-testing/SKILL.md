# Web Application Testing Skill

Source: https://github.com/anthropics/skills/tree/main/skills/webapp-testing

## Overview

This skill provides a Python/Playwright-based toolkit for testing and interacting with local web applications. It enables automated frontend testing, UI verification, and debugging of web interfaces.

## When to Use

Apply this skill when:
- Testing a local web application's UI behavior
- Verifying frontend functionality after changes
- Debugging UI issues that require browser interaction
- Automating user flows for validation
- Capturing screenshots or console logs from the browser

## Decision Framework

```
Is the target static HTML?
├── YES → Read the file directly (no server needed)
└── NO (dynamic app) →
    ├── Is a server already running?
    │   ├── YES → Use Playwright directly
    │   └── NO → Use with_server.py helper
    └── Does it need multiple servers (frontend + backend)?
        └── YES → Use with_server.py with multiple --server flags
```

## Core Tool: Playwright (Python)

Always use `sync_playwright()` for synchronous scripts.

### Basic Pattern

```python
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch()
    page = browser.new_page()
    page.goto("http://localhost:3000")
    
    # CRITICAL: Wait for JS to execute before interacting
    page.wait_for_load_state('networkidle')
    
    # Now safe to inspect/interact
    element = page.query_selector('.my-component')
    print(element.inner_text())
    
    browser.close()
```

### CRITICAL: Always Wait for JS

**Never inspect the DOM without waiting:**
```python
# WRONG - JS may not have executed yet
page.goto("http://localhost:3000")
element = page.query_selector('.dynamic-content')  # may be None!

# CORRECT
page.goto("http://localhost:3000")
page.wait_for_load_state('networkidle')  # wait for JS
element = page.query_selector('.dynamic-content')  # safe now
```

## Server Management: with_server.py

For apps that need a server started automatically:

```bash
# Single server
python scripts/with_server.py \
  --server "npm run dev" --port 3000 \
  -- python my_test.py

# Multiple servers (e.g., backend + frontend)
python scripts/with_server.py \
  --server "python api/server.py" --port 8000 \
  --server "npm run dev" --port 3000 \
  -- python my_test.py
```

**Always run scripts with `--help` first** to understand usage before reading source.

## Reconnaissance Pattern

Before automating, discover the selectors:

```python
# Step 1: Explore the page structure
page.goto("http://localhost:3000")
page.wait_for_load_state('networkidle')

# Print all interactive elements
buttons = page.query_selector_all('button, a, input, [role="button"]')
for el in buttons:
    print(f"Tag: {el.tag_name()}, Text: {el.inner_text()[:50]}, Class: {el.get_attribute('class')}")

# Step 2: Use discovered selectors in automation
```

## Common Testing Patterns

### Screenshot capture
```python
page.screenshot(path="screenshot.png", full_page=True)
```

### Console log capture
```python
page.on("console", lambda msg: print(f"Browser: {msg.text}"))
```

### Form interaction
```python
page.fill('input[name="email"]', 'test@example.com')
page.click('button[type="submit"]')
page.wait_for_load_state('networkidle')
```

### Assert element exists
```python
assert page.query_selector('.success-message') is not None, "Success message not shown"
```

## Best Practices

- Treat bundled helper scripts as black boxes — invoke via CLI, don't read source
- Use `networkidle` state for SPAs, `domcontentloaded` for simpler pages
- Always close the browser when done (use context managers)
- Run `--help` on any unfamiliar script before using it
- Prefer `query_selector_all` for discovery, specific selectors for automation
