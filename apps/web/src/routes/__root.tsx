import { Outlet, createRootRoute } from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/react-router-devtools";
import { Notch } from "@/components/Notch";
import { DynamicIslandToaster } from "@/components/DynamicIsland";
import posthog from "posthog-js";
import { PostHogProvider } from "posthog-js/react";
import { useRef } from "react";

posthog.init(import.meta.env.VITE_PUBLIC_POSTHOG_KEY, {
  api_host: "/relay-lBZL",
  ui_host: import.meta.env.VITE_PUBLIC_POSTHOG_HOST,
  defaults: "2025-05-24",
  autocapture: false,
});

function RootComponent() {
  const notchRef = useRef<HTMLDivElement>(null);

  return (
    <PostHogProvider client={posthog}>
      <Notch ref={notchRef} />

      <DynamicIslandToaster notchRef={notchRef} />

      <Outlet />
      <TanStackRouterDevtools />
    </PostHogProvider>
  );
}

export const Route = createRootRoute({
  component: RootComponent,
});
