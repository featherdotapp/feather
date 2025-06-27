import TestComponent from "@repo/ui/test";
import { createFileRoute } from "@tanstack/react-router";
import { usePostHog } from "posthog-js/react";

export const Route = createFileRoute("/")({
  component: App,
});

function App() {
  const posthog = usePostHog();

  function onclick() {
    posthog.capture("user_pressed_button", {
      source: "frontend",
    });
  }

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">PostHog Test</h1>
      <button
        onClick={onclick}
        className=" hover:bg-blue-700 bg-brand text-white font-bold py-2 px-4 rounded disabled:bg-red-500"
        disabled={!posthog.__loaded}
      >
        Fire Posthog Event
      </button>
      <div className="absolute z-50 flex items-center justify-center w-64 h-64">
        <TestComponent />
      </div>
    </div>
  );
}
