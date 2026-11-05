# WorkManager arrives only through Glance (ADR-0011). It builds its Room database by calling the
# generated class's no-argument constructor by reflection. R8 full mode keeps the class but removes
# that constructor unless it is named, and the app then crashes in its start-up provider before the
# first screen. The launch smoke test in CI starts the shrunk build to catch this kind of gap.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
