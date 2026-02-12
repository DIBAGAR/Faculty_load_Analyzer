import React from "react";

const WorkHistoryTable = ({ data }) => {
  return (
    <table border="1" style={{ width: "100%", marginBottom: "20px" }}>
      <thead>
        <tr>
          <th>Work ID</th>
          <th>Title</th>
          <th>Completed On</th>
          <th>Remarks</th>
        </tr>
      </thead>
      <tbody>
        {data.map((work) => (
          <tr key={work.id}>
            <td>{work.id}</td>
            <td>{work.title}</td>
            <td>{work.completedOn}</td>
            <td>{work.remarks}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default WorkHistoryTable;
