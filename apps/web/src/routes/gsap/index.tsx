import { dynamicIsland } from "@/components/DynamicIsland";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/gsap/")({
  component: RouteComponent,
});

function RouteComponent() {
  return (
    <div className=" flex flex-col items-center justify-center gap-6 p-12 mt-20">
      <button
        onClick={() =>
          dynamicIsland("Simulated Toast Notification", { duration: 4000 })
        }
        className="bg-gray-800 hover:bg-gray-900 text-white font-medium px-6 py-3 rounded-lg shadow-lg transition-all duration-200 hover:scale-105 active:scale-95"
      >
        Simulate Toast
      </button>
    </div>
  );
}
