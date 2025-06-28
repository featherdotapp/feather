import { statusNotification } from "@feather/notification-handler";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/gsap/")({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <div className=" flex flex-col items-center justify-center gap-6">
      <button
        onClick={() =>
          statusNotification("Simulated Toast Notification", { duration: 4000 })
        }
        className="bg-gray-800 hover:bg-gray-900 mt-40 text-white font-medium px-6 py-3 rounded-lg shadow-lg transition-all duration-200 hover:scale-105 active:scale-95"
      >
        Simulate Toast
      </button>
    </div>
  );
}
