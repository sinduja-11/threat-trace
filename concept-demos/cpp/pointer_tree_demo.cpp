/**
 * Threat Trace - Syllabus C++ Concept Demonstration Module
 * 
 * Demonstrates low-level programming concepts that cannot be naturally 
 * expressed in Java:
 * 1. Raw Pointers and Double Pointers (ProcessNode* and ProcessNode**)
 * 2. Manual Memory Management (new and delete)
 * 3. RAII & Destructors (~ProcessNode)
 * 4. Abstract Base Classes & Virtual Functions (polymorphism)
 * 5. Function Pointers (EventCallback)
 */

#include <iostream>
#include <string>
#include <vector>

// 1. Function Pointer Definition
typedef void (*EventLoggerCallback)(const std::string& message);

void defaultConsoleLogger(const std::string& message) {
    std::cout << "[CPP_CALLBACK_LOG] " << message << std::endl;
}

// 2. Abstract Base Class for Polymorphism Demonstration
class ThreatAnalyzerBase {
public:
    virtual ~ThreatAnalyzerBase() {} // Virtual Destructor
    virtual void analyzeEvent(const std::string& eventName) = 0; // Pure Virtual Function
};

class MemoryForensicAnalyzer : public ThreatAnalyzerBase {
public:
    void analyzeEvent(const std::string& eventName) override {
        std::cout << "[MEMORY_ANALYZER] Scanning process buffer memory for: " << eventName << std::endl;
    }
};

// 3. Process Tree Node with Pointers, Double Pointers, and RAII Destructor
class ProcessTreeNode {
public:
    int eventId;
    std::string processName;
    ProcessTreeNode* parentNode;            // Single Pointer
    ProcessTreeNode** childrenNodes;        // Pointer to Pointer Array (Double Pointer)
    int childCount;
    int capacity;

    // Constructor allocating dynamic memory
    ProcessTreeNode(int id, const std::string& name, ProcessTreeNode* parent = nullptr) 
        : eventId(id), processName(name), parentNode(parent), childCount(0), capacity(5) {
        
        // Manual allocation using 'new'
        childrenNodes = new ProcessTreeNode*[capacity];
        for (int i = 0; i < capacity; ++i) {
            childrenNodes[i] = nullptr;
        }
    }

    // RAII Destructor for automatic recursive memory cleanup using 'delete'
    ~ProcessTreeNode() {
        std::cout << "[RAII_DESTRUCTOR] Cleaning up ProcessTreeNode ID: " << eventId << " (" << processName << ")" << std::endl;
        for (int i = 0; i < childCount; ++i) {
            delete childrenNodes[i]; // Recursive delete
        }
        delete[] childrenNodes; // Delete allocated pointer array
    }

    void addChildNode(ProcessTreeNode* child) {
        if (childCount < capacity) {
            childrenNodes[childCount++] = child;
        }
    }
};

// Main function demonstrating pointer operations
int main() {
    std::cout << "=========================================================" << std::endl;
    std::cout << "  THREAT TRACE C++ SYLLABUS CONCEPT DEMONSTRATION MODULE  " << std::endl;
    std::cout << "=========================================================" << std::endl;

    // Demonstration of Function Pointers
    EventLoggerCallback logger = defaultConsoleLogger;
    logger("Initializing C++ Pointer & Memory Management Module.");

    // Demonstration of Virtual Functions & Polymorphism
    ThreatAnalyzerBase* analyzer = new MemoryForensicAnalyzer(); // Polymorphic Pointer
    analyzer->analyzeEvent("powershell.exe -EncodedCommand");
    delete analyzer; // Manual delete

    std::cout << "\n--- Building Pointer-Based Process Tree ---" << std::endl;

    // Building Root Process Node using 'new'
    ProcessTreeNode* root = new ProcessTreeNode(1, "WINWORD.EXE");
    ProcessTreeNode* child1 = new ProcessTreeNode(2, "cmd.exe", root);
    ProcessTreeNode* child2 = new ProcessTreeNode(3, "powershell.exe", child1);

    root->addChildNode(child1);
    child1->addChildNode(child2);

    // Inspecting Double Pointer References
    std::cout << "Root Process Name: " << root->processName << std::endl;
    std::cout << "Child 1 Name via Double Pointer (*root->childrenNodes): " << root->childrenNodes[0]->processName << std::endl;
    std::cout << "Child 2 Name via Double Pointer Depth 2: " << root->childrenNodes[0]->childrenNodes[0]->processName << std::endl;

    std::cout << "\n--- Triggering RAII Destructor Deallocation ---" << std::endl;
    delete root; // Triggers recursive ~ProcessTreeNode() freeing all child memory

    std::cout << "C++ Memory cleanup completed successfully." << std::endl;
    return 0;
}
