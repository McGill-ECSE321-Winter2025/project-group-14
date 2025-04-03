import React from "react";

function Button({ children, type = "default", rounded = false, seamless = false, onClick }) {
    let classNames = "btn";
    if (type !== "default") classNames += ` ${type}`;
    if (rounded) classNames += " rounded";
    if (seamless) classNames += " seamless";

    return (
        <button className={classNames} onClick={onClick}>
            {children}
        </button>
    );
}

export default Button;
/*
<Button>Default Button</Button>
<Button type="success">Success</Button>
<Button type="danger">Delete</Button>
<Button rounded>Rounded Button</Button>
<Button seamless>Seamless Button</Button>
*/