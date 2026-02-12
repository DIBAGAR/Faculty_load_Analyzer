import React from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer } from "recharts";

const PerformanceChart = ({ data }) => {
  // Convert object to array if backend sends { "2026-02-10": 5, ... }
  const chartData = Object.keys(data).map((key) => ({
    date: key,
    hours: data[key],
  }));

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={chartData}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="date" />
        <YAxis />
        <Tooltip />
        <Bar dataKey="hours" fill="#8884d8" />
      </BarChart>
    </ResponsiveContainer>
  );
};

export default PerformanceChart;
