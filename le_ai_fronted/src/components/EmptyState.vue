<script setup>
import { computed } from 'vue'
import { MODE_LOVE, MODE_META } from '../constants/modes'
import ChatAvatar from './ChatAvatar.vue'

const props = defineProps({
  mode: {
    type: String,
    required: true,
  },
})

defineEmits(['suggest'])

const meta = computed(() => MODE_META[props.mode] || MODE_META[MODE_LOVE])
</script>

<template>
  <section class="empty-state" :class="`empty-state--${mode}`">
    <div class="empty-state__orb" :class="`empty-state__orb--${mode}`">
      <ChatAvatar type="assistant" :mode="mode" :size="56" />
    </div>

    <p class="empty-state__eyebrow">
      {{ mode === MODE_LOVE ? 'LOVE MASTER' : 'LE MANUS AGENT' }}
    </p>
    <h2 class="empty-state__title">{{ meta.emptyTitle }}</h2>
    <p class="empty-state__desc">{{ meta.emptyText }}</p>

    <div class="suggestion-grid">
      <button
        v-for="item in meta.suggestions"
        :key="item"
        class="suggestion"
        type="button"
        @click="$emit('suggest', item)"
      >
        <span>{{ item }}</span>
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="M5 12h13m-5.2-5.2L18 12l-5.2 5.2"
            stroke="currentColor"
            stroke-width="1.7"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>

    <p class="empty-state__note">{{ meta.emptyNote }}</p>
  </section>
</template>
