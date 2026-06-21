package engine;

/**
 * All possible game states.
 *   IDLE – no game in progress
 *   EXPLORING – hero is moving through the map
 *   COMBAT – hero is fighting a monster
 *   LOOT_PENDING – hero stepped on a treasure and must decide equip/discard
 *   LEVEL_UP – hero advanced to the next level and must distribute stat points
 *   LEVEL_COMPLETE – hero reached the exit; waiting to proceed
 *   GAME_OVER – hero died
 *   VICTORY – hero went through all levels
 */
public enum State {
    IDLE,
    EXPLORING,
    COMBAT,
    LOOT_PENDING,
    LEVEL_UP,
    LEVEL_COMPLETE,
    GAME_OVER,
    VICTORY
}
