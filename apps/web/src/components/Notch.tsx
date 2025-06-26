import React from "react";

// Notch component for root-level usage
interface NotchProps {
  text?: string;
}

export const Notch = React.forwardRef<HTMLDivElement, NotchProps>(
  ({ text }, ref) => {
    return (
      <div className="flex  flex-col items-center w-full ">
        <div className="w-full h-4 bg-black"></div>
        <div
          ref={ref}
          id="notch"
          className="w-40 h-7 bg-black rounded-b-2xl relative overflow-hidden flex items-center justify-center -mt-1"
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

Notch.displayName = "Notch";
