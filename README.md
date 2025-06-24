# ❄️ frosted-nameplates

> [!NOTE]
> 🚧 Frosted Nameplates is currently in alpha. Expect bugs or other issues.

**Requires [PacketEvents](https://modrinth.com/plugin/packetevents) and [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)**\
**Supported versions:** 1.19.4 and above (tested on 1.21.4)

**Frosted Nameplates** is a [Spigot](https://www.spigotmc.org/) plugin that allows you to fully modify the text shown above the player's head,
including the player's name and hex color. It works by sending packets that create an invisible Text Display entity with a custom name,
that's then being set as the passenger of the player. By using the custom name to display the text instead of the Text Display itself,
the nameplate achieves visuals that are closer to the classic vanilla label.

<div align="center">
  <img alt="Showcase of a customized nameplate" src="https://i.imgur.com/HADn5DR.png" />
</div>

## ⌨️ Commands

All actions are available under the `/frostednameplates` (shortly `/fnp`) command.

| Command       | Permission                  | Description                        |
|---------------|-----------------------------|------------------------------------|
| `/fnp reload` | `frostednameplates.command` | Reloads the plugin's configuration |
| `/fnp update` | `frostednameplates.command` | Updates all nameplates             |

## ⚙️ Configuration

The configuration can be found under `config/FrostedNameplates/config.yml` and contains the following options:

```yml
# The customized nameplate shown above the player's head.
# Supports placeholders from PlaceholderAPI.
#
# Default: '%player_name%'
nameplate: '%player_name%'

# The interval in ticks between nameplate updates.
# Anything below 1 will turn off updates completely.
# 20 ticks = 1 second
#
# Default: 20
update-interval: 20
```

## ❓ Found a bug?

Report it in the [GitHub issue tracker](https://github.com/pandier/frosted-nameplates/issues/new)!

## 📜 License

The content of this repository is licensed under the [MIT license](https://github.com/pandier/frosted-nameplates/blob/main/LICENSE).