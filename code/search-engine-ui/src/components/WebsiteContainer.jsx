import React from "react";
import "../CSS/WebsiteContainer.css"
import BoldedParagraph from "./BoldedParagraph";

function WebsiteContainer({ url, title, queryParagraph, paramValue }) {

    const myStyle = {
        overflow: "hidden",
        textOverflow: "ellipsis",
    };


    function boldWordsInString(mainString, boldString) {
        // Split the boldString into an array of words
        const boldWords = boldString.split(" ");

        // Map over the words in the mainString and wrap them in a <strong> tag if they match a word in boldWords
        const boldedMainString = mainString.split(" ").map((word, index, arr) => {
            if (boldWords.includes(word)) {
                return <strong key={index}>{word} </strong>;
            } else {
                // Check if it's the last word in the array, if so don't add a space
                if (index === arr.length - 1) {
                    return <span key={index}>{word}</span>;
                } else {
                    return <span key={index}>{word} </span>;
                }
            }
        });

        // Return the boldedMainString wrapped in a <p> tag
        return (
            <div style={{ maxHeight: '3.6em', overflow: 'auto' }}>
                <p style={{ myStyle, maxHeight: '100%', overflow: 'hidden', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', display: '-webkit-box' }}>
                    {boldedMainString}
                </p>
            </div>
        );

    }



    return (
        <div id="webcontainer">
            <a href={url}>{title}</a>
            <h2>{url}</h2>
            {boldWordsInString(queryParagraph, paramValue)}.
        </div>
    );

}

export default WebsiteContainer;