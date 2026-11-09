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

# WorkManager builds each work request's input merger by reflection with its no-argument
# constructor. R8 full mode drops that constructor, every Glance session worker then fails before it
# starts, and placed widgets stay on their loading layout.
-keep class * extends androidx.work.InputMerger {
    <init>();
}

# Glance finds the placed copies of a widget by its GlanceAppWidget class name. R8 full mode merged
# the eight widget classes into three, so a refresh drew one widget's content into another (a Today
# and upcoming widget turned into a Today widget after a theme change). Kept classes are not merged.
-keep class * extends androidx.glance.appwidget.GlanceAppWidget {
    <init>();
}
