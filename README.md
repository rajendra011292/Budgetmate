# BudgetMate — zero-sum budgeting (Android, Kotlin + Jetpack Compose)

Open this folder in Android Studio (Koala+), let Gradle sync, run on a device/emulator (API 26+).

## How zero-sum works here
* **Income** (and opening balances) land in **Ready to Assign**.
* You **assign** that money to envelopes (and savings goals). You can't assign more than you have.
* **Spending** reduces an envelope. Overspending is flagged red — **cover** it by moving money.
* Envelope balances **roll over** month to month.
* Goal: **Ready to Assign = ₹0** — every rupee has a job.
* Identity shown in Reports: `Accounts total = Ready + Envelopes + Goals`.

## Structure
* `data/Models.kt` – Room entities/DAO (schema unchanged: your existing data keeps working)
* `data/Engine.kt` – pure zero-sum maths, formatting, parsing
* `BudgetViewModel.kt` – all rules/validation
* `ui/` – theme, components, sheets, screens
