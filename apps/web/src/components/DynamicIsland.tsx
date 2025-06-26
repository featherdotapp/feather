import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import React, { useCallback, useEffect, useRef, useState } from "react";

import type { ReactNode } from "react";

// Types
type DynamicIslandTypes = "default";

interface ExternalDynamicIsland {
  id?: string | number;
  content?: ReactNode;
  duration?: number;
  variant?: DynamicIslandTypes;
  icon?: ReactNode;
  dismissible?: boolean;
}

interface DynamicIslandT extends ExternalDynamicIsland {
  id: string | number;
  content: ReactNode;
  dismissible: boolean;
}

interface DynamicIslandToDismiss {
  id: string | number;
  dismiss: boolean;
}

let islandCounter = 1;

// Observer class (based on Sonner's pattern)
class Observer {
  subscribers: Array<(island: DynamicIslandT | DynamicIslandToDismiss) => void>;
  islands: Array<DynamicIslandT | DynamicIslandToDismiss>;
  dismissedIslands: Set<string | number>;

  constructor() {
    this.subscribers = [];
    this.islands = [];
    this.dismissedIslands = new Set();
  }

  subscribe = (
    subscriber: (island: DynamicIslandT | DynamicIslandToDismiss) => void
  ) => {
    this.subscribers.push(subscriber);

    return () => {
      const index = this.subscribers.indexOf(subscriber);
      this.subscribers.splice(index, 1);
    };
  };

  publish = (data: DynamicIslandT | DynamicIslandToDismiss) => {
    this.subscribers.forEach((subscriber) => subscriber(data));
  };

  addIsland = (data: DynamicIslandT) => {
    this.publish(data);
    this.islands = [...this.islands, data];
  };

  create = (data: ExternalDynamicIsland) => {
    const { content, ...rest } = data;
    const id =
      typeof data?.id === "number" || (data.id && data.id.toString().length > 0)
        ? data.id
        : islandCounter++;

    const alreadyExists = this.islands.find((island) => island.id === id);
    const dismissible = data.dismissible ?? true;

    if (this.dismissedIslands.has(id)) {
      this.dismissedIslands.delete(id);
    }

    if (alreadyExists) {
      this.islands = this.islands.map((island) => {
        if (island.id === id) {
          const updatedIsland = {
            ...island,
            ...data,
            id,
            content,
            dismissible,
          };
          this.publish(updatedIsland);
          return updatedIsland;
        }
        return island;
      });
    } else {
      this.addIsland({ content: content || "", ...rest, dismissible, id });
    }

    return id;
  };

  dismiss = (id?: number | string) => {
    if (id) {
      this.dismissedIslands.add(id);
      requestAnimationFrame(() =>
        this.subscribers.forEach((subscriber) =>
          subscriber({ id, dismiss: true })
        )
      );
    } else {
      this.islands.forEach((island) => {
        this.dismissedIslands.add(island.id);
        this.subscribers.forEach((subscriber) =>
          subscriber({ id: island.id, dismiss: true })
        );
      });
    }
    return id;
  };

  message = (content: ReactNode, data?: ExternalDynamicIsland) => {
    return this.create({ ...data, content });
  };

  getActiveIslands = () => {
    return this.islands.filter(
      (island) => !this.dismissedIslands.has(island.id)
    );
  };
}

export const DynamicIslandState = new Observer();

// Variant styles
const variantStyles = {
  default: {
    bg: "bg-black/90",
    text: "text-white",
    icon: "bg-blue-400",
  },
};

// Main Dynamic Island Component
interface DynamicIslandProps {
  island: DynamicIslandT | null;
  notchRef?: React.RefObject<HTMLDivElement | null>;
  onComplete: () => void;
}

const DynamicIsland: React.FC<DynamicIslandProps> = ({
  island,
  notchRef,
  onComplete,
}) => {
  const islandRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const timelineRef = useRef<gsap.core.Timeline | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const hideAnimation = useCallback(() => {
    // Clear any existing timeout
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }

    if (!timelineRef.current) return;

    const tl = gsap.timeline({
      onComplete: onComplete,
    });

    tl.to(contentRef.current, {
      opacity: 0,
      scale: 0.8,
      duration: 0.2,
      ease: "power2.in",
    }).to(
      islandRef.current,
      {
        width: 160,
        height: 28,
        y: 0,
        scale: 0.3,
        opacity: 0,
        duration: 0.4,
        ease: "back.in(1.2)",
      },
      "-=0.1"
    );
  }, [onComplete]);

  const triggerAnimation = useCallback(() => {
    if (!island) return;

    // Clear any existing timeout and timeline
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }

    if (timelineRef.current) {
      timelineRef.current.kill();
    }

    const tl = gsap.timeline({
      paused: false,
    });

    timelineRef.current = tl;

    // Reset to initial state
    gsap.set(islandRef.current, {
      width: 160,
      height: 28,
      y: 0,
      opacity: 1,
      scale: 1,
      borderRadius: "0 0 16px 16px",
      transformOrigin: "center top",
    });

    gsap.set(contentRef.current, {
      opacity: 0,
      scale: 0.8,
    });

    // Only animate notch if ref is provided
    if (notchRef?.current) {
      gsap.set(notchRef.current, {
        scaleY: 1,
        scaleX: 1,
      });
    }

    // Animate entrance
    const timeline = tl;

    // Phase 0: Notch anticipation (only if notch ref exists)
    if (notchRef?.current) {
      timeline.to(notchRef.current, {
        scaleY: 1.1,
        scaleX: 1.02,
        duration: 0.2,
        ease: "power2.out",
      });
    }

    // Phase 1: Morph and move
    timeline.to(islandRef.current, {
      y: 80,
      borderRadius: "28px",
      duration: 0.5,
      ease: "power3.out",
    });

    if (notchRef?.current) {
      timeline
        .to(
          notchRef.current,
          {
            scaleY: 0.9,
            scaleX: 0.98,
            duration: 0.3,
            ease: "power2.inOut",
          },
          "-=0.4"
        )
        .to(
          notchRef.current,
          {
            scaleY: 1,
            scaleX: 1,
            duration: 0.4,
            ease: "elastic.out(1, 0.5)",
          },
          "-=0.1"
        );
    }

    // Phase 2: Resize
    timeline
      .to(
        islandRef.current,
        {
          width: 320,
          height: 56,
          duration: 0.6,
          ease: "power4.out",
        },
        "-=0.2"
      )
      // Phase 3: Content fade in
      .to(
        contentRef.current,
        {
          opacity: 1,
          scale: 1,
          duration: 0.4,
          ease: "power2.out",
        },
        "-=0.3"
      )
      // Phase 4: Settle pulse
      .to(
        islandRef.current,
        {
          scale: 1.02,
          duration: 0.3,
          ease: "power2.inOut",
          yoyo: true,
          repeat: 1,
        },
        "+=0.1"
      );

    // Set up auto-hide timeout after animation completes
    const animationDuration = 2000; // Approximate total animation duration
    const hideDelay = island.duration ?? 3000;

    timeoutRef.current = setTimeout(() => {
      hideAnimation();
    }, animationDuration + hideDelay);
  }, [island, notchRef, hideAnimation]);

  // Cleanup timeouts on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      if (timelineRef.current) {
        timelineRef.current.kill();
      }
    };
  }, []);

  useGSAP(() => {
    if (island) {
      triggerAnimation();
    } else if (timeoutRef.current) {
      // Clear timeout when island is removed
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, [island, triggerAnimation]);

  if (!island) return null;

  const styles = variantStyles[island.variant ?? "default"];

  return (
    <>
      {/* Dynamic Island */}
      <div className="fixed top-0 left-1/2 transform -translate-x-1/2 z-40">
        <div
          ref={islandRef}
          className={`${styles.bg} backdrop-blur-xl rounded-full shadow-2xl  flex items-center justify-center relative overflow-hidden`}
          style={{
            boxShadow:
              "0 20px 40px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.05)",
          }}
        >
          <div
            ref={contentRef}
            className={`${styles.text} text-sm font-medium flex items-center gap-3 px-4`}
          >
            {island.icon ?? (
              <div
                className={`w-2 h-2 ${styles.icon} rounded-full animate-pulse`}
              />
            )}
            <span className="max-w-64 truncate">{island.content}</span>
          </div>
        </div>
      </div>
    </>
  );
};

// Toaster Component (renders the islands)
interface DynamicIslandToasterProps {
  notchRef?: React.RefObject<HTMLDivElement | null>;
}

export const DynamicIslandToaster: React.FC<DynamicIslandToasterProps> = ({
  notchRef,
}) => {
  const [currentIsland, setCurrentIsland] = useState<DynamicIslandT | null>(
    null
  );

  useEffect(() => {
    const unsubscribe = DynamicIslandState.subscribe((island) => {
      if ((island as DynamicIslandToDismiss).dismiss) {
        setCurrentIsland(null);
      } else {
        setCurrentIsland(island as DynamicIslandT);
      }
    });

    return unsubscribe;
  }, []);

  const handleComplete = useCallback(() => {
    setCurrentIsland(null);
  }, []);

  return (
    <DynamicIsland
      island={currentIsland}
      notchRef={notchRef}
      onComplete={handleComplete}
    />
  );
};

// Main toast function
const islandFunction = (content: ReactNode, data?: ExternalDynamicIsland) => {
  return DynamicIslandState.create({
    content,
    ...data,
  });
};

const basicIsland = islandFunction;
const getHistory = () => DynamicIslandState.islands;
const getIslands = () => DynamicIslandState.getActiveIslands();

// Export main API (Sonner-style)
export const dynamicIsland = Object.assign(
  basicIsland,
  {
    message: DynamicIslandState.message,
    dismiss: DynamicIslandState.dismiss,
  },
  { getHistory, getIslands }
);
