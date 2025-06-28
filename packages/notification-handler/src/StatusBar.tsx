import React from "react";

// StatusBar component for root-level usage
interface StatusBarProps {
  text?: string;
}

export const StatusBar = React.forwardRef<HTMLDivElement, StatusBarProps>(
  ({ text }, ref) => {
    return (
      <div className="flex  flex-col items-center w-full ">
        <div className="w-full h-4 bg-brand"></div>
        <div
          ref={ref}
          id="statusbar"
          className="w-40 h-7 bg-brand rounded-b-2xl relative overflow-hidden flex items-center justify-center -mt-1"
        >
          {text && (
            <span className="text-white text-xs font-medium truncate px-2">
              {text}
            </span>
          )}
        </div>
      </div>
    );
  }
);

StatusBar.displayName = "StatusBar";
