import React, { useEffect, useState } from "react";

// StatusBar text management types
interface StatusBarTextData {
  text: string;
  id?: string | number;
}

interface StatusBarTextToDismiss {
  id: string | number;
  dismiss: boolean;
}

let statusBarCounter = 1;

// Observer class for StatusBar text management
class StatusBarObserver {
  subscribers: Array<
    (data: StatusBarTextData | StatusBarTextToDismiss) => void
  >;
  currentText: StatusBarTextData | null;

  constructor() {
    this.subscribers = [];
    this.currentText = null;
  }

  subscribe = (
    subscriber: (data: StatusBarTextData | StatusBarTextToDismiss) => void
  ) => {
    this.subscribers.push(subscriber);

    return () => {
      const index = this.subscribers.indexOf(subscriber);
      this.subscribers.splice(index, 1);
    };
  };

  publish = (data: StatusBarTextData | StatusBarTextToDismiss) => {
    this.subscribers.forEach((subscriber) => subscriber(data));
  };

  setText = (text: string, id?: string | number) => {
    const textId = id ?? statusBarCounter++;
    const textData: StatusBarTextData = {
      text,
      id: textId,
    };

    this.currentText = textData;
    this.publish(textData);
    return textId;
  };

  clearText = (id?: string | number) => {
    if (id) {
      this.publish({ id, dismiss: true });
    } else if (this.currentText?.id !== undefined) {
      this.publish({ id: this.currentText.id, dismiss: true });
    }
    this.currentText = null;
  };

  getCurrentText = () => {
    return this.currentText;
  };
}

export const StatusBarState = new StatusBarObserver();

// StatusBar component for root-level usage
interface StatusBarProps {
  text?: string;
}

export const StatusBar = React.forwardRef<HTMLDivElement, StatusBarProps>(
  ({ text }, ref) => {
    const [currentText, setCurrentText] = useState<string | undefined>(text);

    useEffect(() => {
      const unsubscribe = StatusBarState.subscribe((data) => {
        if ((data as StatusBarTextToDismiss).dismiss) {
          setCurrentText(undefined);
        } else {
          setCurrentText((data as StatusBarTextData).text);
        }
      });

      return unsubscribe;
    }, []);

    // Update current text when prop changes
    useEffect(() => {
      setCurrentText(text);
    }, [text]);

    const displayText = currentText;

    return (
      <div className="flex  flex-col items-center w-full z-50">
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

          {displayText && (
            <span className="text-white text-xs font-medium truncate px-2">
              {displayText}
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

// Main status bar function API (similar to statusNotification)
const statusBarFunction = (text: string, id?: string | number) => {
  return StatusBarState.setText(text, id);
};

// Export main API
export const statusBar = Object.assign(statusBarFunction, {
  setText: StatusBarState.setText,
  clearText: StatusBarState.clearText,
  getCurrentText: StatusBarState.getCurrentText,
});
