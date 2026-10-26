# Boost V6 source staging for AndroidAPS dev

**Status: source snapshot only — not an installed or active APS plugin.**

These files reproduce the Boost V6 sources from `agniabakush/AAPS-3.4.2.3_Dmitry`, branch `3.4.2.6-Boost_V6_261026` at commit `5f0f9656de046cdab4c5e5616c0f08fa460a19c9`. Paths beneath `legacy/` mirror the source tree; the snapshot is outside any Gradle source set.

The destination `nightscout/AndroidAPS dev` fork now uses Kotlin Multiplatform (AGP 9) and Metro, with `src/commonMain` and `src/androidMain`. The older Boost plugin uses Android-only `src/main`, different preference keys, UI classes, and Hilt/Dagger injection. Simply dropping the files into the active module would break compilation and could create unsafe insulin dosing behavior.

**No Boost algorithm is registered. This snapshot cannot command an insulin pump.**

## Required integration

1. Adapt core algorithms, OapsProfileBoost data types, and new commonMain/Android source sets.
2. Port the needed keys, defaults, and UI/Metro registration without changing existing AAPS defaults.
3. Adapt Nightscout, Health Connect, sensors, pump and APS safety constraints.
4. Preserve Dmitry's upstream Autosens/ISF correction order (never divide by the ratio twice).
5. Run dose-safety replay and unit and integration tests before any plugin is enabled.

The original `dev` APS engine remains unchanged. This staged source is for engineering review, **not clinical use**.


## Kotlin Multiplatform adapter in dev (2026-10-26)

The legacy SHA-verified snapshot remains intact under `legacy/`. An additional, independently compilable module `:plugins:boost-port` is now included from `settings.gradle` and `:app`. It imports the exact source implementations of these *pure* Boost V6/V5 components into `src/commonMain`: NightWindow, StepFeed, MealHypothesis, SafetyGates, ConfirmTrancheController, MealSignalScore, MealActionMultiplier and AggressionBudget. The telemetry-only `Math.round` call in ConfirmTrancheController was converted to Kotlin multiplatform `roundToLong`.

Metro contributes `BoostPortInspectionPlugin` in the GENERAL plugin bucket at order key 235. It is deliberately **hidden and forcibly disabled**, is not an APS, never calls the pump, and only registers a namespaced `BoostPortKey.AuditRequested` boolean defaulting to `false`. It must **not** be advertised as the finished Boost dosing plugin. The original Nightscout APS selection and all default dosing settings remain unchanged. The new module deliberately does not register an APS implementation or alter `OpenAPSSMBPlugin` / `DetermineBasalSMB`.

The source Boost plugin's 2026 Android/Hilt APIs, legacy preference tables, profile and GlucoseStatus types, Nightscout and Health Connect integration, and active dosing path are **not yet ported** to dev's Metro/KMP/Compose APIs. These require explicit data-mapping decisions and dose-safety replays. Dmitry's Autosens/ISF upstream correction must be preserved without a second division, as requested.

The `BoostPortMigrationTest` smoke tests check the default-off flag and pure guard invariants. They do not establish dosing safety, a working pump path or complete V6 parity. Build and test status must be checked in GitHub Actions.

The four unchanged Boost model and metadata JSON blobs were also packaged into `app/src/main/assets/boost/` from the SHA-verified legacy snapshot. Packaging files does **not** load them, enable ML decisions, or alter the default APS engine. The risk model and meal model runtime loaders and all related safety tests still need a target-API migration before activation.


## TDD and V6 transition policy port (2026-10-26)

The source-matched `BoostTddSettingTransition.kt` is now compiled in `src/commonMain` with `BoostTddTransitionPortTest` in `src/commonTest`. This adapts the pure TDD-off, V1-to-V6, and first-enable policy to the new Kotlin Multiplatform module without modifying the active APS plugin or its user preferences. The existing namespaced `BoostPortKey.AuditRequested` is still OFF by default, and `BoostPortInspectionPlugin` remains registered in Metro as GENERAL / forcibly disabled, **not** APS. No new dosing preferences have been surfaced or activated in Nightscout.

The production adapter is not yet integrated: the old plugin is Android/Hilt-specific while Nightscout dev is KMP/Metro. Connecting `DetermineBasalBoost` or `DetermineBasalBoostV5` into live insulin control would require mapping the current shared APS objects and handling source `OapsProfileBoost`, Nightscout history backfill, Health Connect, plugin lifecycle, Compose settings and extensive dose-safety regressions. The legacy snapshot remains isolated to avoid shadowing live classes. Preserve the early Dmitry Autosens/ISF application; do not apply a second static-ratio correction.

**Status: compatibility port in progress; not a functioning/verified Boost APS option.**
