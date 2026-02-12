import React, { useEffect, useState } from "react";
import axios from "../api/axiosConfig";
import AssignedWorkTable from "../components/AssignedWorkTable";
import WorkHistoryTable from "../components/WorkHistoryTable";
import PerformanceChart from "../components/PerformanceChart";

const FacultyDashboard = () => {
  const [assignedWork, setAssignedWork] = useState([]);
  const [history, setHistory] = useState([]);
  const [performance, setPerformance] = useState({});

  useEffect(() => {
    // Fetch assigned work
    axios.get("/faculty/assigned-work").then((res) => setAssignedWork(res.data));

    // Fetch work history
    axios.get("/faculty/work-history").then((res) => setHistory(res.data));

    // Fetch performance
    axios.get("/faculty/performance").then((res) => setPerformance(res.data));
  }, []);

  return (
    <div style={{ padding: "20px" }}>
      <h1>Faculty Dashboard</h1>
      <h2>Assigned Work</h2>
      <AssignedWorkTable data={assignedWork} />

      <h2>Work History</h2>
      <WorkHistoryTable data={history} />

      <h2>Performance (Hours per Day)</h2>
      <PerformanceChart data={performance} />
    </div>
  );
};

export default FacultyDashboard;
