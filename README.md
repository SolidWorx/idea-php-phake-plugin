# PHPStorm Phake Plugin

![Build](https://github.com/SolidWorx/idea-php-phake-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

PHPStorm plugin for the [Phake](https://github.com/phake/phake) mocking framework to allow autocompletion for stubs and mocks.

## Features

### Autocomplete for mock objects

When creating mock objects using `Phake::mock()`, you can get autocomplete results for the object that is being mocked.
PHPStorm will see the variable returned from the `mock()` call to be an instance of the actual class being mocked, which means no more inspections about incomatible types.

<img width="584" alt="Screenshot 2021-07-17 at 17 13 29" src="https://user-images.githubusercontent.com/144858/126041511-ae2cf121-5c45-4001-8ba1-2d3817e897fc.png">

### Autocomplete for method stub

Using `Phake::when`, you can get autocomplete results for the mocked class, so that you don't have to copy-paste method names or manually type them, and ensure there are no typo's in the method names.

<img width="700" alt="Screenshot 2021-07-17 at 17 13 50" src="https://user-images.githubusercontent.com/144858/126041607-48ed9e31-e2a3-4fe6-8edb-e6f94da5c1a8.png">

### Autocomplete for stub return calls

When stubbing method calls, you can get autocomplete results for the stubber proxy value to determine the return value for the method call.

<img width="888" alt="Screenshot 2021-07-17 at 17 14 02" src="https://user-images.githubusercontent.com/144858/126041643-3053fc7f-c12f-4f10-a3f8-89d078c6352e.png">

### Autocomplete for verification methods

Calls to `Phake::verify` will return autocomplete results for the mocked class to ensure you can quickly choose the correct method name.

<img width="686" alt="Screenshot 2021-07-17 at 17 14 58" src="https://user-images.githubusercontent.com/144858/126041691-f165bd68-ebda-4ef4-9885-6b9112b07b14.png">

### Refactor methods will update the mocked classes

When you refactor any method name, all the corresponding methods will automatically be updated iin the mocks you use, so you never have to manually update your methods stubs again.

![Kapture 2021-07-18 at 09 01 05](https://user-images.githubusercontent.com/144858/126058620-cd09f42a-5a98-4f2a-953b-59c3c8217cef.gif)

### Resolving symbols

PHPStorm can correctly resolve all the symbols in the mocks, letting you  click through to the specific methods.

![Kapture 2021-07-18 at 09 04 21](https://user-images.githubusercontent.com/144858/126058711-fa6b3646-1b5c-4488-b4d6-ddb2d2ab9f3d.gif)

