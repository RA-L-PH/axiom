package com.rc.axiom.playback

object Playback {
    // Custom commands
    const val TOGGLE_SHUFFLE = "com.rc.axiom.command.shuffle.toggle"
    const val CYCLE_REPEAT = "com.rc.axiom.command.repeat.cycle"
    const val TOGGLE_FAVORITE = "com.rc.axiom.command.toggle_favorite"
    const val RESTORE_PLAYBACK = "com.rc.axiom.command.restore_playback"

    const val SET_UNSHUFFLED_ORDER = "com.rc.axiom.command.set.unshuffled_order"
    const val SET_STOP_POSITION = "com.rc.axiom.command.set.stop_position"

    // Custom events
    const val EVENT_MEDIA_CONTENT_CHANGED = "com.rc.axiom.event.media_content_changed"
    const val EVENT_FAVORITE_CONTENT_CHANGED = "com.rc.axiom.event.favorite_content_changed"
    const val EVENT_PLAYBACK_RESTORED = "com.rc.axiom.event.playback_restored"
    const val EVENT_PLAYBACK_STARTED = "com.rc.axiom.event.playback_started"
}