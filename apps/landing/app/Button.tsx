"use client";

import posthog from "posthog-js";
import React from "react";

export default function Button() {
  function handleClick() {
    console.log("Button clicked!");
    posthog.capture("marketing_event", {
      source: "frontend",
    });
  }
  return <button onClick={handleClick}>Trigger Marketing event</button>;
}
