import { Outlet, createRootRoute } from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/react-router-devtools";
import posthog from "posthog-js";
import { PostHogProvider } from "posthog-js/react";

posthog.init(import.meta.env.VITE_PUBLIC_POSTHOG_KEY, {
  api_host: "/relay-lBZL",
  ui_host: import.meta.env.VITE_PUBLIC_POSTHOG_HOST,
  defaults: "2025-05-24",
  autocapture: false,
});

function RootComponent() {
  return (
    <PostHogProvider client={posthog}>
      <Outlet />
      <TanStackRouterDevtools />
    </PostHogProvider>
  );
}

export const Route = createRootRoute({
  component: RootComponent,
});
