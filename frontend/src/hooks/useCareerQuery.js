import { useCallback, useEffect, useState } from "react";
import {
  careerErrorMessage,
  getCareerData,
} from "../api/careerEvidenceApi";

export default function useCareerQuery(path) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(Boolean(path));
  const [error, setError] = useState("");
  const [revision, setRevision] = useState(0);

  const reload = useCallback(() => {
    setRevision((value) => value + 1);
  }, []);

  useEffect(() => {
    if (!path) {
      setData(null);
      setLoading(false);
      setError("");
      return;
    }

    const controller = new AbortController();
    let active = true;

    setLoading(true);
    setError("");
    setData(null);

    getCareerData(path, controller.signal)
      .then((result) => {
        if (active) {
          setData(result);
        }
      })
      .catch((requestError) => {
        if (active && !controller.signal.aborted) {
          setError(careerErrorMessage(requestError));
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
      controller.abort();
    };
  }, [path, revision]);

  return {
    data,
    loading,
    error,
    reload,
  };
}