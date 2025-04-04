import denizImg from "../../assets/team/deniz.jpeg";
import ayhamImg from "../../assets/team/ayham.jpeg";
import reinaImg from "../../assets/team/reina.jpeg";
import mathisImg from "../../assets/team/mathis.jpeg";
import kassemImg from "../../assets/team/kassem.jpeg";
import hamzaImg from "../../assets/team/hamza.jpeg";
import marianneImg from "../../assets/team/marianne.jpeg";

import React from "react";

function About() {
    const team = [
        {
            name: "Deniz Kuru",
            role: "UI/UX Designer",
            description: "Designs intuitive interfaces, creates mockups, and ensures smooth user experience across the app.",
            image: denizImg,
        },
        {
            name: "Ayham Nassar",
            role: "Back-End Developer",
            description: "Implements robust data management and ensures seamless integration between front-end and back-end.",
            image: ayhamImg,
        },
        {
            name: "Reina El-Hoz",
            role: "Front-End Developer",
            description: "Builds responsive user interfaces and integrates front-end components with back-end services.",
            image: reinaImg,
        },
        {
            name: "Mathis Bélanger",
            role: "Project Manager & Software Developer",
            description: "Coordinates development efforts, manages sprints, and contributes to the overall software architecture.",
            image: mathisImg,
        },
        {
            name: "Kassem Yassine",
            role: "Full-Stack Developer",
            description: "Works across the full stack to deliver scalable and secure features with solid integration between layers.",
            image: kassemImg,
        },
        {
            name: "Hamza Abudaqa",
            role: "Test & Verification Developer",
            description: "Ensures the app runs reliably by conducting rigorous testing and maintaining test documentation.",
            image: hamzaImg,
        },
        {
            name: "Marianne Romero",
            role: "Software Systems Integration Specialist",
            description: "Ensures smooth feature integration and promotes best practices across all technologies used.",
            image: marianneImg,
        },
    ];
    
    return (
        <div className="about-container px-4 py-8 max-w-4xl mx-auto">
            <h1 className="text-3xl font-bold text-center mb-6">About GameNight</h1>
            <p className="text-center text-lg mb-10">
                GameNight is a fun application made especially for board game enthusiasts!
                <br /><br />
                Select board games to borrow from a large selection, and lend your own games to other users.
                <br /><br />
                Look for gaming events you can join, and organize your own.
            </p>

            <h2 className="text-2xl font-semibold text-center mb-6">Meet the Team</h2>
            <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-6">
                {team.map((member, index) => (
                    <div key={index} className="flex flex-col items-center text-center p-4 shadow rounded-lg bg-white">
                        <img 
                            src={member.image} 
                            alt={member.name}
                            className="w-24 h-24 rounded-full object-cover mb-4 border-2 border-gray-200"
                        />
                        <h3 className="text-lg font-semibold">{member.name}</h3>
                        <p className="text-sm italic mb-2">{member.role}</p>
                        <p className="text-sm text-gray-600">{member.description}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default About;