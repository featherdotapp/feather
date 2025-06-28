import React from "react";

// StatusBar component for root-level usage
interface StatusBarProps {
  text?: string;
}

export const StatusBar = React.forwardRef<HTMLDivElement, StatusBarProps>(
  ({ text }, ref) => {
    return (
      <div className="flex  flex-col items-center w-full ">
        <div className="w-full h-1 bg-brand" />

        <div
          ref={ref}
          id="statusbar"
          className="w-40 h-9 bg-brand rounded-b-lg relative  -mt-1 flex items-center justify-center"
        >
          <svg
            width="5"
            height="5"
            viewBox="0 0 5 5"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className="absolute -left-[3px] top-0.5 stroke-brand stroke-2"
          >
            <path d="M0 1C1.5 1 4 2 4 5" />
          </svg>

          {text && (
            <span className="text-white text-xs font-medium truncate px-2">
              {text}
            </span>
          )}

          <svg
            width="5"
            height="5"
            viewBox="0 0 5 5"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className="absolute -right-[3px] top-0.5 stroke-brand stroke-2"
          >
            <path d="M5 1C3.5 1 1 2 1 5" />
          </svg>
        </div>
      </div>
    );
  }
);

StatusBar.displayName = "StatusBar";
