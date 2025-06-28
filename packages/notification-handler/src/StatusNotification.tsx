import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import React, { useCallback, useEffect, useRef, useState } from "react";

import type { ReactNode } from "react";

// Types
type StatusNotificationTypes = "default";

interface ExternalStatusNotification {
  id?: string | number;
  content?: ReactNode;
  duration?: number;
  variant?: StatusNotificationTypes;
  icon?: ReactNode;
  dismissible?: boolean;
}

interface StatusNotificationT extends ExternalStatusNotification {
  id: string | number;
  content: ReactNode;
  dismissible: boolean;
}

interface StatusNotificationToDismiss {
  id: string | number;
  dismiss: boolean;
}

let notificationCounter = 1;

// Observer class (based on Sonner's pattern)
class Observer {
  subscribers: Array<
    (notification: StatusNotificationT | StatusNotificationToDismiss) => void
  >;
  notifications: Array<StatusNotificationT | StatusNotificationToDismiss>;
  dismissedNotifications: Set<string | number>;

  constructor() {
    this.subscribers = [];
    this.notifications = [];
    this.dismissedNotifications = new Set();
  }

  subscribe = (
    subscriber: (
      notification: StatusNotificationT | StatusNotificationToDismiss
    ) => void
  ) => {
    this.subscribers.push(subscriber);

    return () => {
      const index = this.subscribers.indexOf(subscriber);
      this.subscribers.splice(index, 1);
    };
  };

  publish = (data: StatusNotificationT | StatusNotificationToDismiss) => {
    this.subscribers.forEach((subscriber) => subscriber(data));
  };

  addNotification = (data: StatusNotificationT) => {
    this.publish(data);
    this.notifications = [...this.notifications, data];
  };

  create = (data: ExternalStatusNotification) => {
    const { content, ...rest } = data;
    const id =
      typeof data?.id === "number" || (data.id && data.id.toString().length > 0)
        ? data.id
        : notificationCounter++;

    const alreadyExists = this.notifications.find(
      (notification) => notification.id === id
    );
    const dismissible = data.dismissible ?? true;

    if (this.dismissedNotifications.has(id)) {
      this.dismissedNotifications.delete(id);
    }

    if (alreadyExists) {
      this.notifications = this.notifications.map((notification) => {
        if (notification.id === id) {
          const updatedNotification = {
            ...notification,
            ...data,
            id,
            content,
            dismissible,
          };
          this.publish(updatedNotification);
          return updatedNotification;
        }
        return notification;
      });
    } else {
      this.addNotification({
        content: content || "",
        ...rest,
        dismissible,
        id,
      });
    }

    return id;
  };

  dismiss = (id?: number | string) => {
    if (id) {
      this.dismissedNotifications.add(id);
      requestAnimationFrame(() =>
        this.subscribers.forEach((subscriber) =>
          subscriber({ id, dismiss: true })
        )
      );
    } else {
      this.notifications.forEach((notification) => {
        this.dismissedNotifications.add(notification.id);
        this.subscribers.forEach((subscriber) =>
          subscriber({ id: notification.id, dismiss: true })
        );
      });
    }
    return id;
  };

  message = (content: ReactNode, data?: ExternalStatusNotification) => {
    return this.create({ ...data, content });
  };

  getActiveNotifications = () => {
    return this.notifications.filter(
      (notification) => !this.dismissedNotifications.has(notification.id)
    );
  };
}

export const StatusNotificationState = new Observer();

// Variant styles
const variantStyles = {
  default: {
    bg: "bg-[rgba(252,100,50,0.1)]",
    text: "text-[#ffe2d9]",
    icon: "bg-[#fc6432]",
    border: "border-[rgba(252,100,50,0.01)]",
  },
};

// Main Status Notification Component
interface StatusNotificationProps {
  notification: StatusNotificationT | null;
  statusBarRef?: React.RefObject<HTMLDivElement | null>;
  onComplete: () => void;
}

const StatusNotification: React.FC<StatusNotificationProps> = ({
  notification,
  statusBarRef,
  onComplete,
}) => {
  const notificationRef = useRef<HTMLDivElement>(null);
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
      notificationRef.current,
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
    if (!notification) return;

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
    gsap.set(notificationRef.current, {
      width: 160,
      height: 28,
      y: 0,
      opacity: 1,
      scale: 1,
      borderRadius: "0 0 45px 45px",
      transformOrigin: "center top",
    });

    gsap.set(contentRef.current, {
      opacity: 0,
      scale: 0.8,
    });

    // Only animate status bar if ref is provided
    if (statusBarRef?.current) {
      gsap.set(statusBarRef.current, {
        scaleY: 1,
        scaleX: 1,
      });
    }

    // Animate entrance
    const timeline = tl;

    // Phase 0: Status bar anticipation (only if status bar ref exists)
    if (statusBarRef?.current) {
      timeline.to(statusBarRef.current, {
        scaleY: 1.1,
        scaleX: 1.02,
        duration: 0.2,
        ease: "power2.out",
      });
    }

    // Phase 1: Morph and move
    timeline.to(notificationRef.current, {
      y: 80,
      borderRadius: "45px",
      duration: 0.5,
      ease: "power3.out",
    });

    if (statusBarRef?.current) {
      timeline
        .to(
          statusBarRef.current,
          {
            scaleY: 0.9,
            scaleX: 0.98,
            duration: 0.3,
            ease: "power2.inOut",
          },
          "-=0.4"
        )
        .to(
          statusBarRef.current,
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
        notificationRef.current,
        {
          width: 280,
          height: 40,
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
        notificationRef.current,
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
    const hideDelay = notification.duration ?? 3000;

    timeoutRef.current = setTimeout(() => {
      hideAnimation();
    }, animationDuration + hideDelay);
  }, [notification, statusBarRef, hideAnimation]);

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
    if (notification) {
      triggerAnimation();
    } else if (timeoutRef.current) {
      // Clear timeout when notification is removed
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, [notification, triggerAnimation]);

  if (!notification) return null;

  const styles = variantStyles[notification.variant ?? "default"];

  return (
    <>
      {/* Status Notification */}
      <div className="fixed top-0 left-1/2 transform -translate-x-1/2">
        <div
          ref={notificationRef}
          className={`${styles.bg} backdrop-blur-xl rounded-[45px] shadow-[0px_4px_8px_0px_inset_rgba(255,255,255,0.05),0px_-1px_4px_0px_inset_rgba(252,100,50,0.3)] flex items-center justify-center relative overflow-hidden`}
          style={{
            border: "1px solid rgba(252,100,50,0.01)",
          }}
        >
          <div
            ref={contentRef}
            className={`${styles.text} text-[12px] font-medium flex items-center gap-1 px-3 py-2 leading-[16px]`}
            style={{
              fontFamily: '"Suisse Int\'l", sans-serif',
              fontWeight: 500,
            }}
          >
            {notification.icon ?? (
              <div
                className={`w-4 h-4 ${styles.icon} rounded-full flex-shrink-0`}
                style={{
                  backgroundImage:
                    'url("http://localhost:3845/assets/d450a09cbef2d2b34e49c49995c535bab61cb101.svg")',
                  backgroundSize: "contain",
                  backgroundRepeat: "no-repeat",
                  backgroundPosition: "center",
                }}
              />
            )}
            <span className="max-w-64 truncate whitespace-nowrap">
              {notification.content}
            </span>
          </div>
        </div>
      </div>
    </>
  );
};

// Toaster Component (renders the notifications)
interface StatusNotificationToasterProps {
  statusBarRef?: React.RefObject<HTMLDivElement | null>;
}

export const StatusNotificationToaster: React.FC<
  StatusNotificationToasterProps
> = ({ statusBarRef }) => {
  const [currentNotification, setCurrentNotification] =
    useState<StatusNotificationT | null>(null);

  useEffect(() => {
    const unsubscribe = StatusNotificationState.subscribe((notification) => {
      if ((notification as StatusNotificationToDismiss).dismiss) {
        setCurrentNotification(null);
      } else {
        setCurrentNotification(notification as StatusNotificationT);
      }
    });

    return unsubscribe;
  }, []);

  const handleComplete = useCallback(() => {
    setCurrentNotification(null);
  }, []);

  return (
    <StatusNotification
      notification={currentNotification}
      statusBarRef={statusBarRef}
      onComplete={handleComplete}
    />
  );
};

// Main notification function
const notificationFunction = (
  content: ReactNode,
  data?: ExternalStatusNotification
) => {
  return StatusNotificationState.create({
    content,
    ...data,
  });
};

const basicNotification = notificationFunction;
const getHistory = () => StatusNotificationState.notifications;
const getNotifications = () => StatusNotificationState.getActiveNotifications();

// Export main API (Sonner-style)
export const statusNotification = Object.assign(
  basicNotification,
  {
    message: StatusNotificationState.message,
    dismiss: StatusNotificationState.dismiss,
  },
  { getHistory, getNotifications }
);

// Export types
export type {
  StatusNotificationTypes,
  ExternalStatusNotification,
  StatusNotificationToasterProps,
};
