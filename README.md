# ❄️ frosted-nameplates

> [!NOTE]
> 🚧 Frosted Nameplates is currently in alpha. Expect bugs or other issues.

**Requires [PacketEvents](https://modrinth.com/plugin/packetevents)**\
**Supported versions:** Paper 1.20.6 and above (tested on 1.21.11)

**Frosted Nameplates** is a [Paper](https://papermc.io/) plugin that allows you to fully modify the text shown above the player's head,
including the player's name and hex color. It works by sending packets that create an invisible Text Display entity with a custom name,
that's then being set as the passenger of the player. By using the custom name to display the text instead of the Text Display itself,
the nameplate achieves visuals that are closer to the classic vanilla label.

Supports [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

<div align="center">
  <img alt="Showcase of a customized nameplate" src="https://i.imgur.com/HADn5DR.png" />
</div>

## ⌨️ Commands

All actions are available under the `/frostednameplates` (shortly `/fnp`) command.

| Command                 | Permission                  | Description                                 |
|-------------------------|-----------------------------|---------------------------------------------|
| `/fnp reload`           | `frostednameplates.command` | Reloads the plugin's configuration          |
| `/fnp update`           | `frostednameplates.command` | Updates all nameplates                      |
| `/fnp update <players>` | `frostednameplates.command` | Updates nameplates of the specified players |

## ⚙️ Configuration

The configuration can be found under `config/FrostedNameplates/config.yml` and contains the following options:

```yml
# The customized nameplate shown above the player's head.
# Supports placeholders from PlaceholderAPI and MiniPlaceholders (requires minimessage formatter).
#
# Default: '%player_name%'
nameplate: '%player_name%'

# The formatter used for formatting the nameplate.
# Available options:
#   minimessage = Uses Adventure's MiniMessage format (recommended, supports MiniPlaceholders)
#   legacy = Uses legacy colors (&)
#
# Default: minimessage
formatter: minimessage

# The interval in ticks between nameplate updates.
# Anything below 1 will turn off automatic updates completely.
# 20 ticks = 1 second
#
# Default: 20
update-interval: 20
```

## ❓ Found a bug?

Report it in the [GitHub issue tracker](https://github.com/pandier/frosted-nameplates/issues/new)!

## 📜 License

The content of this repository is licensed under the [MIT license](https://github.com/pandier/frosted-nameplates/blob/main/LICENSE).