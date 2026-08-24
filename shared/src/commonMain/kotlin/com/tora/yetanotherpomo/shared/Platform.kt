package com.tora.yetanotherpomo.shared

/**
 * The smallest possible demonstration of the only mechanism KMP gives you for platform code.
 *
 * `expect` is a declaration with NO body: common code may call [platformName] freely, and the
 * compiler simply promises that every target supplies one. If a target forgets, the build fails -
 * this is a compile-time contract, not a runtime lookup.
 */
expect fun platformName(): String
