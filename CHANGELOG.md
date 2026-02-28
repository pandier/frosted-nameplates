# Changelog

All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0-alpha.7] - 2026-02-28

### Added

- Updating nameplates of specific players using `/fnp update <players>`

### Changed

- Switched to a Paper plugin

### Removed

- Remove `hybrid` formatter, which didn't even work, and proper implementation is out of scope right now

### Fixed

- Complete rewrite of the backend with proper synchronization, hopefully this fixes a lot of niche bugs

## [0.1.0-alpha.6] - 2025-12-19

### Fixed

- Nameplates detaching when a player receives a passenger

## [0.1.0-alpha.5] - 2025-07-11

### Added

- Warnings for unexpected states

### Fixed

- Attempt to fix issues with ghost nameplates

## [0.1.0-alpha.4] - 2025-07-03

### Fixed

- Some NullPointerExceptions

## [0.1.0-alpha.3] - 2025-06-25

### Fixed

- Concurrent issues
- NullPointerExceptions sometimes when disconnecting

## [0.1.0-alpha.2] - 2025-06-25

### Added

- Different formatting options (Legacy, MiniMessage)
- Support for MiniPlaceholders

### Changed

- Plugin now only supports Paper
- PlaceholderAPI dependency is now optional

## [0.1.0-alpha.1] - 2025-06-24

Initial release

[Unreleased]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.7...HEAD
[0.1.0-alpha.7]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.6...v0.1.0-alpha.7
[0.1.0-alpha.6]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.5...v0.1.0-alpha.6
[0.1.0-alpha.5]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.4...v0.1.0-alpha.5
[0.1.0-alpha.4]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.3...v0.1.0-alpha.4
[0.1.0-alpha.3]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.2...v0.1.0-alpha.3
[0.1.0-alpha.2]: https://github.com/pandier/frosted-nameplates/compare/v0.1.0-alpha.1...v0.1.0-alpha.2
[0.1.0-alpha.1]: https://github.com/pandier/frosted-nameplates/commits/v0.1.0-alpha.1
