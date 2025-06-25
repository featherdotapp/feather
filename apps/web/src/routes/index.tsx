import { createFileRoute } from "@tanstack/react-router";
import { usePostHog } from "posthog-js/react";

export const Route = createFileRoute("/")({
  component: App,
});

function App() {
  const posthog = usePostHog();

  function onclick() {
    posthog.capture("user_pressed_button", {
      demoVersion: true,
    });
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">PostHog Test</h1>
      <button
        onClick={onclick}
        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:bg-red-500"
        disabled={!posthog.__loaded}
      >
        Fire Posthog Event
      </button>
    </div>
  );
}
