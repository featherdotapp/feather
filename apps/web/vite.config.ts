import tailwindcss from "@tailwindcss/vite";
import viteReact from "@vitejs/plugin-react";
import { defineConfig } from "vite";

import tanstackRouter from "@tanstack/router-plugin/vite";
import { resolve } from "node:path";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    tanstackRouter({ autoCodeSplitting: true }),
    viteReact(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      "@": resolve(__dirname, "./src"),
    },
  },
  server: {
    proxy: {
      // Rule for /relay-lBZL/static/:path*
      "/relay-lBZL/static/": {
        target: "https://eu-assets.i.posthog.com",
        changeOrigin: true, // Needed for virtual hosting
        secure: true, // Target is HTTPS
        rewrite: (path) => path.replace(/^\/relay-lBZL\/static/, "/static"),
      },
      // Rule for /relay-lBZL/flags
      "/relay-lBZL/flags": {
        target: "https://eu.i.posthog.com",
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/relay-lBZL/, ""), // Removes /relay-lBZL
      },
      // General rule for /relay-lBZL/:path*
      // This should be the last one, as more specific rules should come first.
      "/relay-lBZL/": {
        target: "https://eu.i.posthog.com",
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^\/relay-lBZL/, ""), // Removes /relay-lBZL
      },
    },
  },
});
