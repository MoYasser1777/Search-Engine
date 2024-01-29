import React, { useState, useEffect, useRef } from 'react';
import '../CSS/Home2.css';
import '@fortawesome/fontawesome-free/css/all.min.css';
import { Link } from 'react-router-dom';
import TextField from '@mui/material/TextField';
import Autocomplete from '@mui/material/Autocomplete';
import SpeechRecognition, { useSpeechRecognition } from 'react-speech-recognition';

function Home() {
    const [text, setText] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [queries, setQueries] = useState(["top", "last"]);

    const inputRef = useRef(null);
    const {
        transcript,
        listening,
        resetTranscript,
        browserSupportsSpeechRecognition
    } = useSpeechRecognition();

    const [counter, setCounter] = useState(0);

    useEffect(() => {
        if (text && showSuggestions) {
            setSuggestions([counter, counter + 1, counter + 2, counter + 3, counter + 4]);
        } else {
            setSuggestions([]);
            setCounter(0);
        }
    }, [text, showSuggestions, counter]);

    useEffect(() => {
        fetch(`http://localhost:8080/autocomplete`)
            .then((response) => response.json())
            .then((data) => {
                setQueries(data);
            });
    }, []);

    const handleSubmit = (e) => {
        e.preventDefault();
        const encodedText = encodeURIComponent(text);
        window.location.href = "./search?q=" + encodedText;
    };

    const handleMicClick = () => {
        if (!listening) {
            SpeechRecognition.startListening();
        } else {
            SpeechRecognition.stopListening();
        }
    };

    useEffect(() => {
        if (transcript) {
            setText(transcript);
        }
    }, [transcript]);

    if (!browserSupportsSpeechRecognition) {
        return <span>Browser doesn't support speech recognition.</span>;
    }

    return (
        <div id='body'>
            <main>
                <img className='logo' src="https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png" alt="" />
                <div className='search'>
                    <form onSubmit={handleSubmit} style={{ width: "100%" }}>
                        <Autocomplete
                            fullWidth
                            freeSolo
                            id="free-solo-2-demo"
                            onInputChange={(event, newValue) => {
                                setText(newValue);
                            }}
                            value={text}
                            disableClearable
                            options={queries.map((query) => query)}
                            renderInput={(params) => (
                                <TextField
                                    {...params}
                                    label="Search input"
                                    variant="standard"
                                    InputProps={{
                                        ...params.InputProps,
                                        disableUnderline: true,
                                        type: 'search',
                                    }}
                                />
                            )}
                        />
                    </form>
                    <span className="fas fa-microphone" onClick={handleMicClick}>
                        {listening ? 'Stop' : 'Start'}
                    </span>
                </div>

            </main>
        </div>
    );
}

export default Home;
