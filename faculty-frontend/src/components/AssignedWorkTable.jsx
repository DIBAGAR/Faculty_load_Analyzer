import React from "react";

const AssignedWorkTable = ({ data }) => {
  return (
    <table border="1" style={{ width: "100%", marginBottom: "20px" }}>
      <thead>
        <tr>
          <th>Work ID</th>
          <th>Title</th>
          <th>Deadline</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {data.map((work) => (
          <tr key={work.id}>
            <td>{work.id}</td>
            <td>{work.title}</td>
            <td>{work.deadline}</td>
            <td>{work.status}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default AssignedWorkTable;
