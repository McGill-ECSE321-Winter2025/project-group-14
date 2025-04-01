import React from "react";

function Box({ children, dark = false }) {
    return <div className={dark ? "bg-dark" : "bg-white"}>{children}</div>;
}

export default Box;
/*

HOW to use: 
<Box>
    <h2>Light Background Box</h2>
    <p>This is inside a light gray box.</p>
</Box>

<Box dark>
    <h2>Dark Background Box</h2>
    <p>This is inside a dark gray box.</p>
</Box>
*/