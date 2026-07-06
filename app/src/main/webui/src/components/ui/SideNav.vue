<template>
  <nav class="flex h-full flex-col gap-1 p-4">
    <slot name="header" />
    <div class="mt-4 flex flex-col gap-1">
      <slot />
    </div>
    <div class="mt-auto">
      <slot name="footer" />
    </div>
  </nav>
</template>
