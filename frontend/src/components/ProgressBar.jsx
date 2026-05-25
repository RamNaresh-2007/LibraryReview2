import React from 'react';

const ProgressBar = ({ isProgress }) => {
    return (
        isProgress && (
            <div className='progress'>
                <div className='loader'></div>
            </div>
        )
    );
};

export default ProgressBar;
