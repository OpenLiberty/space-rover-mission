/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useState, useEffect, useCallback } from "react";

interface ScriptEntry {
  name: string;
  description: string;
  steps: Array<{ command: string; durationMs: number }>;
}


const BOB_BRIDGE_SCRIPTS_URL = "http://localhost:4000/scripts";

const useScripts = () => {
  const [scripts, setScripts] = useState<ScriptEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchScripts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(BOB_BRIDGE_SCRIPTS_URL);
      if (!res.ok) {
        setError("Could not load scripts from bob-bridge.");
        return;
      }
      const data: ScriptEntry[] = await res.json();
      setScripts(data);
    } catch {
      setError("Could not reach the bob-bridge service. Is it running?");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchScripts();
  }, [fetchScripts]);

  return { scripts, loading, error, refetch: fetchScripts };
};

export default useScripts;
