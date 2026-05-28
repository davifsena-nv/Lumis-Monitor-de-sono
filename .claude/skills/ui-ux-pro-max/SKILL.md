# UI/UX Pro Max Skill

Source: https://github.com/nextlevelbuilder/ui-ux-pro-max-skill

## Overview

UI/UX Pro Max is a comprehensive design intelligence system offering 50+ styles, 161 color palettes, 57 font pairings, 161 product types, 99 UX guidelines, and 25 chart types across 10 technology stacks.

## When to Use

Apply this skill when tasks involve **UI structure, visual design decisions, interaction patterns, or user experience quality control**. Essential for:
- Designing pages and creating components
- Selecting typography/color systems
- Reviewing UI code
- Making product-level design decisions

Skip for pure backend logic, API design, infrastructure work, or non-visual automation tasks.

## 10 Priority Rule Categories

1. **Accessibility** (CRITICAL)
   - Minimum 4.5:1 contrast ratio for normal text
   - Visible focus rings on all interactive elements
   - Descriptive alt text and aria-labels
   - Full keyboard navigation support

2. **Touch & Interaction** (CRITICAL)
   - Touch targets minimum 44×44pt
   - 8px minimum spacing between targets
   - Visual feedback within 100–150ms for all interactions
   - Loading state feedback for async operations

3. **Performance** (HIGH)
   - Image optimization and lazy loading
   - Avoid layout shifts (CLS)
   - Code splitting for large bundles

4. **Style Selection** (HIGH)
   - Match style to product type and audience
   - Consistency across all components
   - Use SVG icons — NEVER emoji as structural icons

5. **Layout & Responsive** (HIGH)
   - Mobile-first design approach
   - Defined breakpoints (mobile, tablet, desktop)
   - No horizontal scroll on any viewport

6. **Typography & Color** (MEDIUM)
   - Line height 1.4–1.6 for body text
   - Semantic color tokens (not hardcoded values)
   - 4/8dp spacing rhythm

7. **Animation** (MEDIUM)
   - Duration 150–300ms for UI transitions
   - Use transform and opacity only (GPU compositing)
   - Respect `prefers-reduced-motion`

8. **Forms & Feedback** (MEDIUM)
   - Visible labels (never placeholder-only)
   - Error messages adjacent to fields
   - Progressive disclosure for complex forms

9. **Navigation Patterns** (HIGH)
   - Bottom navigation ≤5 items on mobile
   - Deep linking support
   - Consistent back navigation behavior

10. **Charts & Data** (LOW)
    - Always include legends and tooltips
    - Accessible color pairs (not color-only distinction)
    - Responsive chart sizing

## Key Workflow

**Step 1:** Analyze requirements (product type, audience, style keywords, tech stack)

**Step 2:** Generate design system (colors, typography, spacing, component tokens)

**Step 3:** Apply domain-specific guidelines based on product type

**Step 4:** Apply stack-specific guidelines (React, React Native, Vue, etc.)

## Common Professional UI Mistakes to AVOID

- **Emoji as icons** — use vector icons (Lucide, Heroicons, react-native-vector-icons)
- **Inconsistent icon stroke width** — pick one weight and stick to it
- **Ignoring safe areas** on mobile (notch, home indicator)
- **Missing pressed states** — all interactive elements need visual feedback
- **Light/dark mode** — test both independently, don't assume one works if the other does
- **Hardcoded colors** — use semantic tokens always

## Pre-Delivery Verification Checklist

Before delivering any UI work, confirm:
- [ ] No emoji used as structural icons
- [ ] All interactive elements have visible feedback
- [ ] Touch targets ≥44×44pt with proper spacing
- [ ] Safe-area compliance on mobile
- [ ] Semantic color tokens used throughout
- [ ] 4/8dp spacing rhythm maintained
- [ ] Text measures are readable (max ~70ch for body)
- [ ] Light AND dark mode tested independently
- [ ] Contrast ratios meet WCAG AA minimum
- [ ] Keyboard navigation works fully
