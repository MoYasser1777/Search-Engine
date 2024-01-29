import React, { useState, useEffect } from 'react';
import WebsiteContainer from './WebsiteContainer';
import { Link } from 'react-router-dom';
import '../CSS/Search.css';
import '@fortawesome/fontawesome-free/css/all.min.css';
import { useLocation } from 'react-router-dom';
import TextField from '@mui/material/TextField';
import Autocomplete from '@mui/material/Autocomplete';
import SpeechRecognition, { useSpeechRecognition } from 'react-speech-recognition';
//import data from '../data';

function Search() {
    const location = useLocation();
    const searchParams = new URLSearchParams(location.search);
    const paramValue = decodeURIComponent(searchParams.get('q'));

    const [text, setText] = useState('');
    const [data, setData] = useState([]);
    const [time, setTime] = useState(0);
    const [numofResults, setNumofResults] = useState(0);
    const [queries, setQueries] = useState([
        "top",
        "last"
    ]);
    const [isLoading, setIsLoading] = useState(true);

    const {
        transcript,
        listening,
        resetTranscript,
        browserSupportsSpeechRecognition
    } = useSpeechRecognition();

    useEffect(() => {
        fetch(`http://localhost:8080/autocomplete`)
            .then((response) => response.json())
            .then((data) => {
                setQueries(data);
            });

    }, [])

    useEffect(() => {
        if (paramValue[0] == '"' && paramValue[paramValue.length - 1] == '"') {
            let stringWithoutQuotes = paramValue.slice(1, -1);
            console.log(stringWithoutQuotes);
            console.log("gowa el double quotes");

            fetch(`http://localhost:8080/phrase?q=${stringWithoutQuotes}`)
                .then((response) => response.json())
                .then((data) => {
                    setData(data.results);
                    setTime(data.time);
                    setNumofResults(data.results.length);
                    setIsLoading(false); // Data is retrieved, set loading to false
                });
        }
        else {
            fetch(`http://localhost:8080/?q=${paramValue}`)
                .then((response) => response.json())
                .then((data) => {
                    setData(data.results);
                    setTime(data.time);
                    setNumofResults(data.results.length);
                    setIsLoading(false); // Data is retrieved, set loading to false
                });
        }

    }, []);

    useEffect(() => {
        fetch(`https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=${text}`)
            .then((response) => response.json())
            .then((data) => {
                console.log(data);
            });
    }, [text]);

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            const encodedText = encodeURIComponent(text);
            window.location.href = "./search?q=" + encodedText;
        }
    };

    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentData = data.slice(startIndex, endIndex);

    const totalPages = Math.ceil(data.length / itemsPerPage);

    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

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
        <div id="holder">

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
            {isLoading ? ( // Display loading icon if still loading
                <div className="loading-icon">
                    <i className="fas fa-spinner fa-spin"></i> Loading...
                </div>
            ) : (
                <h3>About {numofResults} results ({time} seconds) </h3>
            )}



            {currentData.map((item) => (
                <WebsiteContainer
                    key={item.title}
                    title={item.title}
                    url={item.url}
                    queryParagraph={item.queryParagraph}
                    paramValue={paramValue}
                />
            ))}

            <div className="pagination">
                {Array.from({ length: totalPages }, (_, index) => (
                    <button
                        key={index}
                        className={currentPage === index + 1 ? 'active' : ''}
                        onClick={() => handlePageChange(index + 1)}
                    >
                        {index + 1}
                    </button>
                ))}
            </div>
        </div>
    );
}

export default Search;
