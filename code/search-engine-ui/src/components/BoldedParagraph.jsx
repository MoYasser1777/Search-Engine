import React from 'react'

function BoldedParagraph({ sentence, boldWords }) {
    const words = sentence.split(/\s+/);

    const boldedWords = words.map(word => {
        if (boldWords.includes(word)) {
            return <span style={{ fontWeight: 'bold' }}>{word} </span>;
        }
        return `${word} `;
    });

    return <p>{boldedWords}</p>;

}

export default BoldedParagraph