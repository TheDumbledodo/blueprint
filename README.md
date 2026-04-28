# 🔷 Blueprint

[![Build Status](https://img.shields.io/github/actions/workflow/status/TheDumbledodo/blueprint/gradle-publish.yml)](https://github.com/TheDumbledodo/blueprint/actions)
[![Version](https://img.shields.io/github/v/release/TheDumbledodo/blueprint)](https://github.com/TheDumbledodo/blueprint/releases)

Blueprint is a small Java framework for component loading and service access. It is mainly built for Paper and Velocity plugins, but the `blueprint-api` module can be used in any Java project that wants a simple singleton-style service registry through `Services`.

For Minecraft plugins, Blueprint keeps bootstrap code thin by handling component scanning, constructor injection, service lookup, configuration loading, command setup, and common helper utilities.

The project is inspired by [Supervisor](https://github.com/cjcameron92/supervisor), and builds on a few focused libraries:

- [ConfigLib](https://github.com/Exlll/ConfigLib) for YAML configuration
- [Aikar Commands](https://github.com/aikar/commands) for command registration

## Why Blueprint

Most plugins start simple, then slowly collect manual setup code in the main class: configs are loaded there, listeners are registered there, commands are created there, and services are passed around by hand.

Blueprint moves that wiring into a small component system:

```java
public final class ExamplePlugin extends JavaPlugin implements BlueprintModule {

    @Override
    public void onEnable() {
        Blueprint.register(this);
    }
}
```

After registration, components can depend on each other through constructors:

```java
@BlueprintComponent
@AllArgsConstructor
public final class UserController {

    private final UserService userService;
    private final SettingsConfig settings;
}
```

The result is a main class that stays small and feature classes that declare their dependencies directly.

## Modules

| Module | Purpose |
| --- | --- |
| `blueprint-api` | Components, lifecycle, scanning, and the `Services` registry for any Java project |
| `blueprint-configuration` | ConfigLib integration |
| `blueprint-helper` | Text, time, random, data, and other helpers |
| `blueprint-paper` | Paper bootstrap, commands, listeners, and Bukkit serializers |
| `blueprint-velocity` | Velocity bootstrap, commands, and event listeners |
| `blueprint-menu` | PacketEvents menu utilities |

## Basic Usage

For Paper plugins:

```java
public final class ExamplePlugin extends JavaPlugin implements BlueprintModule {

    @Override
    public void onEnable() {
        Blueprint.register(this);
    }
}
```

For Velocity plugins:

```java
@Plugin(id = "example", name = "Example", version = "1.0.0")
public final class ExamplePlugin implements BlueprintModule {

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Blueprint.register(this, server, dataDirectory, logger);
    }
}
```

For plain Java projects, the API module can still be used as a lightweight service registry:

```java
Services.register(UserService.class, new UserService());

UserService users = Services.loadIfPresent(UserService.class);
```
