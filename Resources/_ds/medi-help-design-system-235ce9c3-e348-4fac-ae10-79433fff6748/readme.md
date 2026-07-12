# Medi-Help Design System

Medi-Help is an Android health assistant app that helps people — especially elderly users and those with limited medical knowledge — keep track of their health and understand complicated medical documents. It turns prescriptions and lab reports into plain language, manages medication reminders, and tracks vitals over time, always with clear provenance (date + source) and no diagnostic claims.

## Sources used

- **GitHub repo:** [shopnil13/Medi-Help](https://github.com/shopnil13/Medi-Help) — read `README.md`, `Medi-Help_Requirement_Analysis.md`, and `Implementation_Plan.md`. This repo is a **planning-stage monorepo**: it defines the product requirements, tech stack (Kotlin/Jetpack Compose/Material 3 for Android, FastAPI for backend), phased build plan, and a partial FastAPI auth backend skeleton. **It contains no UI code, Figma file, or component library** — no Android/Compose source exists yet. Explore the repo further yourself for the fullest picture of planned features and data models; it's a good reference for what screens/flows to build next.
- **Brand sheet:** `uploads/medi-help_brand_logo_icon.png` — supplied logo lockups (primary mark, wordmark, app icon, monochrome variants) and the official red color palette with hex values. Source of truth for all logo assets and brand colors in this system.

Because no UI/Figma source existed, this design system's components and UI kit were **authored from scratch** following the requirement analysis, implementation plan (which specifies Material 3 + Jetpack Compose), and the brand sheet — not copied from an existing interface. Treat the UI kit as a plausible, on-brand starting point, not a pixel-accurate recreation of a shipped app.

## Components

Standard primitive set (no source UI library existed to enumerate from), grouped by concern:

- **Core:** Button, IconButton
- **Forms:** TextField, Select, Checkbox, RadioGroup, Switch
- **Surfaces:** Card
- **Feedback:** Badge, Dialog, Snackbar
- **Navigation:** TopAppBar, BottomNavBar
- **Data:** ProvenanceTag

### Intentional additions

- **ProvenanceTag** — not a typical design-system primitive, but mandated by the product's own requirement analysis: *"Every data point plotted on the chart must explicitly display its Date and Source."* Added as a first-class component rather than reimplemented ad hoc in every screen.
- **Dialog** is treated as required (not optional chrome) because of the explicit safety rule: *"Never let AI directly change medication reminders without user confirmation."*

## UI Kit

`ui_kits/android_app/` — an interactive click-through recreation of the Android app: Home dashboard, Medicines list + detail, Health Chart (vitals) + manual entry, Upload & AI-review flow (with required confirmation dialog), Insights, and Settings. Built from the requirement analysis's module breakdown (Document Processing, Medication Management, Health Chart, Insights) since no visual source existed.

## Index

- `styles.css` — root stylesheet, imports everything below
- `tokens/colors.css`, `tokens/typography.css`, `tokens/spacing.css`, `tokens/fonts.css`
- `guidelines/*.card.html` — foundation specimen cards (Colors, Type, Spacing, Brand)
- `components/{core,forms,surfaces,feedback,navigation,data}/` — components described above
- `ui_kits/android_app/` — the Android app UI kit
- `assets/logo/` — logo lockups cropped from the supplied brand sheet
- `SKILL.md` — portable skill definition for use in Claude Code

---

## Content Fundamentals

Voice comes straight from the brand sheet's own copy examples and the requirement analysis's target reading level.

- **Reading level:** every medicine/lab explanation must be understandable to a 10-year-old — short sentences, common words, no jargon left untranslated.
- **Tone:** warm, reassuring, plain-spoken. Brand tagline: *"Smart care. Always there."* Value-prop copy from the brand sheet: *"Caring — We put you first"*, *"Secure — Your data, protected"*, *"Organized — All records, in one place"*, *"Smart Assistant — Helpful. Always."*
- **Person:** second person ("Take 1 tablet after breakfast", "Your blood pressure is trending down") — direct and personal, never clinical third person.
- **Casing:** sentence case for body copy and buttons ("Confirm & Add to My Medicines"); label tags may use small tracked uppercase (e.g. "BLOOD PRESSURE" in the vitals list) as the only stylistic exception.
- **Safety language:** never phrase anything as a diagnosis. Required hedge language: *"This may need medical attention. Please contact a doctor."* and *"These tips are general suggestions, not a medical diagnosis."* Always show a data point's date + source next to any health value.
- **Confirmation copy:** AI/automated actions are framed as offers, not facts — "We found 2 medicines" + "Review and uncheck anything that's not right," never "2 medicines added."
- **Emoji:** not used in the brand sheet or product copy. A small heart glyph (♥) appears once as a decorative accent next to the tagline on the brand sheet — treat this as a one-off brand flourish, not a pattern to repeat.

## Visual Foundations

- **Color:** one warm red ramp (deep oxblood `#620004` → pale blush `#FECBBA`) does almost all the work — see `guidelines/colors-primary.card.html`. Primary actions use `--red-600` (`#CF3324`, "medical coral red"); darker reds are reserved for pressed/hover states and rare dark-surface contexts (e.g. a hero band), never as default backgrounds. Backgrounds are a warm off-white (`#FFF7F4`), not stark white/gray — this is a deliberate "calm, not clinical" choice. Status colors (normal/low/high) intentionally reuse the brand red for "high" rather than inventing a new alert color; green/amber/blue-gray were added only where the brand ramp has no equivalent (see `colors-status.card.html`).
- **Type:** two-family pairing — Manrope (display/headings, semibold–extrabold) for a friendly-but-confident voice, Public Sans (body) for maximum legibility at large sizes; see "Font substitution" note below. Body text is never smaller than 16px anywhere in-product; the default reading size is 18–20px, well above typical app conventions, to serve elderly/low-vision users.
- **Spacing & tap targets:** 4px base scale. Minimum tap target is 56px (buttons, list rows) — never 44px, the usual mobile minimum; this app goes larger deliberately.
- **Corner radii:** Material 3 "expressive" rounding — 12px (chips/small inputs) up to 36px (hero cards), plus true pill radii for buttons and toggles. Nothing in the system uses sharp (0px) corners.
- **Shadows:** soft, warm-tinted (red-black at low opacity, never cool gray) — `guidelines/elevation.card.html`. Three levels: resting card, hover lift, and modal/dialog. No inner shadows, no glassmorphism/blur.
- **Backgrounds:** flat warm off-white or white cards only. No gradients, no photography, no illustration patterns, no textures — the brand sheet itself uses flat color and one gradient-shaded icon (the logo), so product surfaces stay flat and calm rather than decorative.
- **Motion:** minimal and calm — a single standard easing curve (`--ease-standard`) and short durations (120–360ms). No bounce, no springs, no looping decorative animation; this is a medical-trust context, not a playful one. Press states shrink slightly (scale 0.97); hover/press darken toward the next ramp step rather than lightening.
- **Borders:** a single soft border color (`--warm-200` / `#F1D2CC`) outlines cards and inputs at 1–2px; no double borders or colored left-accent bars.
- **Imagery:** none supplied — no photography or illustration exists in the brand materials beyond the logo itself. Do not invent stock photography or illustration; leave imagery slots as plain color blocks/placeholders until real assets arrive.
- **Transparency/blur:** used only for the modal scrim behind Dialog (48% black-red overlay) — never on cards or nav chrome.

### Font substitution notice

No brand type files were supplied. **Manrope** (display) + **Public Sans** (body) were chosen as the nearest Google Fonts matches for a friendly, highly legible, humanist sans that performs well at large accessible sizes — deliberately avoiding default Android Roboto to give the brand its own voice while staying Material-3-compatible. **Please share real brand font files if the brand has typography guidelines beyond this palette**, and this system will be updated to match.

## Iconography

No icon assets (font, sprite, or SVG set) existed in the supplied repo or brand sheet. **Google Material Symbols (Outlined)**, loaded via CDN, was chosen as the substitute — it's the standard companion icon set for Material 3 / Jetpack Compose (the app's own specified UI framework), so icons will look native once implemented in Compose (`Icons.Outlined.*` maps 1:1 to Material Symbols names). Icons are used at 18–26px, tinted brand red for primary actions and secondary-text gray for inline meta icons (see `guidelines/iconography.card.html`). No emoji, no unicode glyphs, no custom-drawn SVG icons are used anywhere in the system.

## Logo

Cropped directly from the supplied brand sheet — no logo was invented. Assets live in `assets/logo/`:

- `medi-help-logomark.png` — icon only (document + stethoscope cross + caring hands + heart)
- `medi-help-logo-wordmark.png` — icon + "Medi-Help / Your Health Assistant" lockup
- `medi-help-app-icon.png` — rounded-square app icon
- `medi-help-logo-mono-dark.png` / `medi-help-logo-mono-gray.png` — single-color variants for constrained contexts
- `medi-help-brand-sheet-full.png` — the full original brand sheet, kept for reference

## Caveats

- No Android/Compose source code or Figma file exists yet for this product — this system is a from-scratch Material-3-flavored authoring effort grounded in the requirement analysis and brand sheet, not a recreation of a shipped screen. Treat the UI kit as a proposed starting point.
- Fonts are a CDN substitution (see above) pending real brand type files.
- Icons are Google Material Symbols via CDN, not a bespoke Medi-Help icon set (none exists yet).
- Status colors (success/warning/info) beyond "high" (brand red) were newly authored in muted oklch hues to sit quietly next to the brand ramp — flag if the brand wants these to look different.

**Please review and iterate with me** — especially: (1) confirm the Android UI kit's screens/flows match what you have in mind for the real app, (2) let me know if real font files or an icon set exist so I can swap out the CDN substitutions, and (3) point me to any Figma files or further along Android code if/when they exist so future passes can be grounded in real source rather than this from-scratch pass.
