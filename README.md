# Swaparoo Plugin

Swaparoo is a Minecraft plugin that implements and manages two new currencies called StarGems and StarDust. It also allows swapping various currencies in-game.

## Placeholders

The plugin integrates with PlaceholderAPI to provide the following placeholders:

- `%swaparoo_server%` - Displays the server name (auto-detected on boot, no config needed).
- `%swaparoo_stargems%` - Displays the number of StarGems a player has.
- `%swaparoo_stardust%` - Displays the number of StarDust a player has.
- `%swaparoo_gemcost_<cost>%` - Displays the formatted stargems cost the item. Replace `<cost>` with the item cost.
- `%swaparoo_msg_<server>_<cost>%` - Legacy purchase check for a specific server and cost. Replace `<server>` with the required server name and `<cost>` with the item cost. Returns `OK` or a failure message. New configs should use `%swaparoo_msg:<token>[:<token>...]%`.
- `%swaparoo_msg:<token>[:<token>...]%` - Runs a chain of purchase checks. See [Purchase check tokens](#purchase-check-tokens).
- `%swaparoo_keys_<keyname>%` - Displays the number of keys a player has for a specific key type. Replace `<keyname>` with the name of the key.

### Purchase check tokens

`%swaparoo_msg:<token>[:<token>...]%` checks tokens from left to right. It returns `OK` if they all pass, otherwise it stops at the first failure and returns that token's message or error code. Results are cached per player and token string for 1 second.

Example: `%swaparoo_msg:cost=100:server=survival:inv=3:creativeplots<10%`

| Token | Passes when | Permission checked |
|---|---|---|
| `cost=<n>` | The player has at least `n` StarGems | |
| `server=<name>` | The current server name matches `<name>` (case-insensitive) | |
| `inv=<n>` | The player has at least `n` free inventory slots | |
| `creativeplots<<n>` | The player's creative plots limit is below `n` | `creativeplots.<value>` (0 to 50) |
| `creativeplots><n>` | The player's creative plots limit is above `n` | `creativeplots.<value>` (0 to 50) |
| `homes<<n>` | The player's homes limit is below `n` | `gesuit.homes.limits.<value>` (0 to 100, in steps of 5) |
| `vaults<<n>` | The player's vaults limit is below `n` | `playervaults.amount.<value>` (0 to 10) |
| `cparkour<<n>` | The player's community parkour plots limit is below `n` | `creativeparkour.create.<value>` (0 to 20) |

In the table, `<n>` is a whole number of 0 or more with no space before it, so a real token looks like `creativeplots<10`.

Error codes:

- `INVALID_FORMAT` - The token list is empty.
- `INVALID_KEY` - A token isn't recognised.
- `INVALID_COST`, `INVALID_SERVER`, `INVALID_INV`, `INVALID_CREATIVEPLOTS`, `INVALID_HOMES`, `INVALID_VAULTS`, `INVALID_CPARKOUR` - The token's value is missing or invalid.
- `PLAYER_OFFLINE` - An `inv` or permission token was checked for a player who isn't online.

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
