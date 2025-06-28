import { Outlet, createRootRoute } from "@tanstack/react-router";
import { TanStackRouterDevtools } from "@tanstack/react-router-devtools";
import {
  StatusNotificationToaster,
  StatusBar,
} from "@feather/notification-handler";
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
  const statusBarRef = useRef<HTMLDivElement>(null);

  return (
    <PostHogProvider client={posthog}>
      <div className="absolute inset-x-0 ">
        <StatusBar ref={statusBarRef} />
        <StatusNotificationToaster statusBarRef={statusBarRef} />
      </div>

      <div className="bg-surface-black min-h-dvh">
        <Outlet />
      </div>
      <TanStackRouterDevtools />
    </PostHogProvider>
  );
}

export const Route = createRootRoute({
  component: RootComponent,
});
