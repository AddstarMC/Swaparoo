# Swaparoo Plugin

Swaparoo is a Minecraft plugin that implements and manages two new currencies called StarGems and StarDust. It also allows swapping various currencies in-game.

## Placeholders

The plugin integrates with PlaceholderAPI to provide the following placeholders:

- `%swaparoo_server%` - Displays the server name (auto-detected on boot, no config needed).
- `%swaparoo_stargems%` - Displays the number of StarGems a player has.
- `%swaparoo_stardust%` - Displays the number of StarDust a player has.
- `%swaparoo_gemcost_<cost>%` - Displays the formatted stargems cost the item. Replace `<cost>` with the item cost.
- `%swaparoo_msg_<server>_<cost>%` - Displays the formatted message for a specific server and cost. Replace `<server>` with the required server name and `<cost>` with the item cost.
- `%swaparoo_keys_<keyname>%` - Displays the number of keys a player has for a specific key type. Replace `<keyname>` with the name of the key.

## Commands

- `/swaparoo reload` - Reloads the plugin configuration.
- `/swaparoo debug` - Toggles the debug mode.
- `/swaparoo keys` - Placeholder for future functionality.
- `/swaparoo stargems <add|remove|set> <player> <amount>` - Manages StarGems for a player.
- `/swaparoo stardust <add|remove|set> <player> <amount>` - Manages StarDust for a player.
- `/stargems` - Displays the player's current balance of StarGems and StarDust.
- `/stargems <player>` - Displays another player's balance of StarGems and StarDust (requires permission).
- `/swaparoo buy <player> <gems> <packagename> <params>` - Purchase a package for a player (normally only used by console).
- `/buyconfirm` - Confirm the purchase of an item (used in clickable messages).

## Permissions

- `swaparoo.command.reload` - Allows the use of the `/swaparoo reload` command.
- `swaparoo.command.debug` - Allows the use of the `/swaparoo debug` command.
- `swaparoo.command.keys` - Allows the use of the `/swaparoo keys` command.
- `swaparoo.command.stargems` - Allows the use of the `/swaparoo stargems` and `/swaparoo stardust` commands.
- `swaparoo.command.balance` - Allows the use of the `/stargems` command to view the player's balance.
- `swaparoo.command.balance.other` - Allows the use of the `/stargems <player>` command to view another player's balance.
- `swaparoo.command.buyconfirm` - Allows the use of the `/buyconfirm` command.

## Integrations

### Treasures

Swaparoo integrates with the Treasures plugin to manage in-game reward keys. The configuration for the database connection is handled through the `hikari.properties` file.

### StarGems/StarDust

Swaparoo implements two custom currencies: StarGems and StarDust. The database configuration for these currencies is handled through the `stargemsdb.properties` file.

## Configuration

The plugin's configuration file allows you to customize various settings. Refer to the generated `config.yml` file for detailed configuration options.
