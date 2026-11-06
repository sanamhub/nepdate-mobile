# WorkManager arrives only through Glance (ADR-0011). It builds its Room database by calling the
# generated class's no-argument constructor by reflection. R8 full mode keeps the class but removes
# that constructor unless it is named, and the app then crashes in its start-up provider before the
# first screen. The launch smoke test in CI starts the shrunk build to catch this kind of gap.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}

# Glance builds each widget click callback by reflection with its no-argument constructor. Its own
# rule keeps the class only, so R8 full mode drops the constructor and widget buttons do nothing.
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    <init>();
}
