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
